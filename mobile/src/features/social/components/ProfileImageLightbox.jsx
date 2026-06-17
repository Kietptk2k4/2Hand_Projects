import { Modal, Pressable, Image, Text, View, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    backdrop: {
      flex: 1,
      backgroundColor: "rgba(0,0,0,0.88)",
      alignItems: "center",
      justifyContent: "center",
      padding: 16,
    },
    closeBtn: {
      position: "absolute",
      right: 16,
      width: 40,
      height: 40,
      borderRadius: 20,
      backgroundColor: "rgba(255,255,255,0.92)",
      alignItems: "center",
      justifyContent: "center",
      zIndex: 2,
    },
    image: {
      width: "100%",
      height: "80%",
    },
    label: {
      marginTop: 12,
      fontSize: 14,
      color: "#FFFFFF",
      textAlign: "center",
    },
  };
}

export function ProfileImageLightbox({ imageUrl, label = "Xem ảnh", onClose }) {
  const insets = useSafeAreaInsets();
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!imageUrl) return null;

  return (
    <Modal visible transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose}>
        <Pressable
          style={[styles.closeBtn, { top: insets.top + 12 }]}
          onPress={onClose}
          accessibilityRole="button"
          accessibilityLabel="Đóng"
        >
          <Ionicons name="close" size={22} color={colors.onSurface} />
        </Pressable>

        <Pressable onPress={(event) => event.stopPropagation()}>
          <Image
            source={{ uri: imageUrl }}
            style={styles.image}
            resizeMode="contain"
            accessibilityLabel={label}
          />
          {label ? <Text style={styles.label}>{label}</Text> : null}
        </Pressable>
      </Pressable>
    </Modal>
  );
}
