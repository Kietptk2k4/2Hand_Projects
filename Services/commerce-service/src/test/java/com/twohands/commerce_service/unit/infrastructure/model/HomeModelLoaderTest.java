package com.twohands.commerce_service.unit.infrastructure.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.home.HomeModelArtifactRepository;
import com.twohands.commerce_service.domain.home.RankingMode;
import com.twohands.commerce_service.infrastructure.model.HomeModelLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeModelLoaderTest {

    @Test
    void shouldInitializeWithMissingFileAsDegraded() {
        HomeModelArtifactRepository repository = mock(HomeModelArtifactRepository.class);
        when(repository.findActive(anyString())).thenReturn(Optional.empty());
        HomeModelLoader loader = new HomeModelLoader(repository, new ObjectMapper());
        ReflectionTestUtils.setField(loader, "modelPath", "missing_home_model.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "commerce_home_ranker");
        ReflectionTestUtils.setField(loader, "modelRoot", "");

        assertThatNoException().isThrownBy(loader::init);

        HomeModelLoader.HomeModelRuntime runtime = loader.resolveRuntime();
        assertThat(runtime.rankingMode()).isEqualTo(RankingMode.DEGRADED);
        assertThat(runtime.fallbackReason()).isEqualTo(HomeModelLoader.REASON_FILE_NOT_FOUND);
    }

    @Test
    void resolveUnderModelRootRejectsTraversal(@TempDir Path tempDir) {
        assertThat(HomeModelLoader.resolveUnderModelRoot(tempDir.toString(), "../escape.onnx")).isNull();
        assertThat(HomeModelLoader.resolveUnderModelRoot(tempDir.toString(), "nested/model.onnx")).isNull();
        assertThat(HomeModelLoader.resolveUnderModelRoot(tempDir.toString(), "commerce_home_ranker_v1.onnx")).isNotNull();
    }

    @Test
    void shouldFallbackToConfiguredPathWhenArtifactBasenameUnsafe(@TempDir Path tempDir) {
        HomeModelArtifactRepository repository = mock(HomeModelArtifactRepository.class);
        when(repository.findActive("commerce_home_ranker")).thenReturn(Optional.of(
                new HomeModelArtifactRepository.ActiveModelArtifact(
                        "commerce_home_ranker",
                        1,
                        "onnx",
                        "../unsafe.onnx"
                )
        ));
        HomeModelLoader loader = new HomeModelLoader(repository, new ObjectMapper());
        ReflectionTestUtils.setField(loader, "modelPath", "missing_home_model.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "commerce_home_ranker");
        ReflectionTestUtils.setField(loader, "modelRoot", tempDir.toString());

        loader.init();

        assertThat(loader.resolveRuntime().fallbackReason()).isEqualTo(HomeModelLoader.REASON_FILE_NOT_FOUND);
    }
}
