package com.twohands.social_service.unit.infrastructure.model;

import com.twohands.social_service.infrastructure.model.ModelLoader;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ModelLoaderTest {

    @Test
    void shouldInitializeWithNullSessionWhenModelFileDoesNotExist() {
        ModelLoader loader = new ModelLoader();
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_model_file.onnx");

        // Should not throw exception and should set session to null
        assertThatNoException().isThrownBy(loader::init);
        assertThat(loader.getSession()).isNull();
        assertThat(loader.getEnv()).isNotNull();
    }

    @Test
    void shouldFallbackGracefullyOnReloadWhenModelFileDoesNotExist() {
        ModelLoader loader = new ModelLoader();
        ReflectionTestUtils.setField(loader, "modelPath", "non_existent_model_file.onnx");
        loader.init();

        assertThatNoException().isThrownBy(loader::reloadModel);
        assertThat(loader.getSession()).isNull();

        assertThatNoException().isThrownBy(loader::forceReload);
        assertThat(loader.getSession()).isNull();
    }
}
