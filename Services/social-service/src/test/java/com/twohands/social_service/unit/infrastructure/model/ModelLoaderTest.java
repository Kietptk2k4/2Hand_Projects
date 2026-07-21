package com.twohands.social_service.unit.infrastructure.model;

import com.twohands.social_service.domain.post.ModelArtifactRepository;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

        assertThatNoException().isThrownBy(loader::init);
        assertThat(loader.getSession()).isNull();
        assertThat(loader.getActiveModelVersion()).isNull();
        assertThat(loader.getEnv()).isNotNull();
    }

    @Test
    void shouldFallbackGracefullyOnReloadWhenModelFileDoesNotExist() {
        ModelArtifactRepository artifactRepository = mock(ModelArtifactRepository.class);
        when(artifactRepository.findActive(anyString())).thenReturn(Optional.empty());

        ModelLoader loader = new ModelLoader(artifactRepository);
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_model_file.onnx");
        ReflectionTestUtils.setField(loader, "modelName", "feed_ranker");
        loader.init();

        assertThatNoException().isThrownBy(loader::reloadModel);
        assertThat(loader.getSession()).isNull();

        assertThatNoException().isThrownBy(loader::forceReload);
        assertThat(loader.getSession()).isNull();
    }
}
