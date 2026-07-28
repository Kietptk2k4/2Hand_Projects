package com.twohands.social_service.unit.infrastructure.model;

import com.twohands.social_service.domain.post.ModelArtifactRepository;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelLoaderTest {

    @Test
    void shouldInitializeWithNullSessionWhenModelFileDoesNotExist() {
        ModelArtifactRepository artifactRepository = mock(ModelArtifactRepository.class);
        when(artifactRepository.findActive(anyString())).thenReturn(Optional.empty());

        ModelLoader loader = new ModelLoader(artifactRepository);
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_model_file.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "feed_ranker");
        ReflectionTestUtils.setField(loader, "modelRoot", "");

        assertThatNoException().isThrownBy(loader::init);
        assertThat(loader.getSession()).isNull();
        assertThat(loader.getActiveModelVersion()).isNull();
        assertThat(loader.getEnv()).isNotNull();
        assertThat(loader.getFallbackReason()).isEqualTo(ModelLoader.REASON_FILE_NOT_FOUND);
    }

    @Test
    void shouldFallbackGracefullyOnReloadWhenModelFileDoesNotExist() {
        ModelArtifactRepository artifactRepository = mock(ModelArtifactRepository.class);
        when(artifactRepository.findActive(anyString())).thenReturn(Optional.empty());

        ModelLoader loader = new ModelLoader(artifactRepository);
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_model_file.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "feed_ranker");
        ReflectionTestUtils.setField(loader, "modelRoot", "");
        loader.init();

        assertThatNoException().isThrownBy(loader::reloadModel);
        assertThat(loader.getSession()).isNull();

        assertThatNoException().isThrownBy(loader::forceReload);
        assertThat(loader.getSession()).isNull();
    }

    @Test
    void isSafeArtifactBasename_acceptsSingleSegment() {
        assertThat(ModelLoader.isSafeArtifactBasename("feed_ranker_v1.onnx")).isTrue();
    }

    @Test
    void isSafeArtifactBasename_rejectsTraversalAndSeparators() {
        assertThat(ModelLoader.isSafeArtifactBasename("../other.onnx")).isFalse();
        assertThat(ModelLoader.isSafeArtifactBasename("a/b.onnx")).isFalse();
        assertThat(ModelLoader.isSafeArtifactBasename("a\\b.onnx")).isFalse();
        assertThat(ModelLoader.isSafeArtifactBasename("")).isFalse();
        assertThat(ModelLoader.isSafeArtifactBasename(null)).isFalse();
    }

    @Test
    void resolveUnderModelRoot_joinsBasename() {
        Path resolved = ModelLoader.resolveUnderModelRoot("/models/recsys", "feed_ranker_v1.onnx");
        assertThat(resolved).isNotNull();
        assertThat(resolved.getFileName().toString()).isEqualTo("feed_ranker_v1.onnx");
        assertThat(resolved.getParent().getFileName().toString()).isEqualTo("recsys");
    }

    @Test
    void resolveUnderModelRoot_rejectsUnsafe() {
        assertThat(ModelLoader.resolveUnderModelRoot("/models/recsys", "../x.onnx")).isNull();
        assertThat(ModelLoader.resolveUnderModelRoot("", "feed_ranker_v1.onnx")).isNull();
        assertThat(ModelLoader.resolveUnderModelRoot("/models/recsys", "D:\\abs.onnx")).isNull();
    }

    @Test
    void shouldReportFileNotFoundWhenActiveBasenameMissingUnderRoot(@TempDir Path tempDir) {
        ModelArtifactRepository artifactRepository = mock(ModelArtifactRepository.class);
        when(artifactRepository.findActive("feed_ranker")).thenReturn(Optional.of(
                new ModelArtifactRepository.ActiveModelArtifact(
                        "feed_ranker", 1, "ONNX", "feed_ranker_v1.onnx"
                )
        ));

        ModelLoader loader = new ModelLoader(artifactRepository);
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_fallback.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "feed_ranker");
        ReflectionTestUtils.setField(loader, "modelRoot", tempDir.toString());

        loader.init();

        assertThat(loader.getSession()).isNull();
        assertThat(loader.getFallbackReason()).isEqualTo(ModelLoader.REASON_FILE_NOT_FOUND);
        assertThat(loader.resolveRuntimeStatus("lightgbm").mode()).isEqualTo("rule_based");
    }

    @Test
    void shouldRejectUnsafeActivePathAndFallBackToMissingModelPath(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("ignored.onnx"), "not-a-real-onnx");

        ModelArtifactRepository artifactRepository = mock(ModelArtifactRepository.class);
        when(artifactRepository.findActive("feed_ranker")).thenReturn(Optional.of(
                new ModelArtifactRepository.ActiveModelArtifact(
                        "feed_ranker", 1, "ONNX", "../escape.onnx"
                )
        ));

        ModelLoader loader = new ModelLoader(artifactRepository);
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_fallback.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "feed_ranker");
        ReflectionTestUtils.setField(loader, "modelRoot", tempDir.toString());

        loader.init();

        assertThat(loader.getSession()).isNull();
        assertThat(loader.getFallbackReason()).isEqualTo(ModelLoader.REASON_FILE_NOT_FOUND);
    }
}
