import { Modal, Pressable, Image, Text, View, FlatList, Dimensions } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

const SCREEN_WIDTH = Dimensions.get("window").width;

export function ProductMediaLightbox({ items, initialIndex = 0, onClose }) {
  const insets = useSafeAreaInsets();
  const colors = useThemeColors();

  if (!items?.length) return null;

  return (
    <Modal visible transparent animationType="fade" onRequestClose={onClose}>
      <View
        style={{
          flex: 1,
          backgroundColor: "rgba(0,0,0,0.92)",
          paddingTop: insets.top,
          paddingBottom: insets.bottom,
        }}
      >
        <Pressable
          onPress={onClose}
          style={{
            position: "absolute",
            top: insets.top + 8,
            right: 16,
            zIndex: 2,
            width: 40,
            height: 40,
            borderRadius: 20,
            backgroundColor: "rgba(255,255,255,0.92)",
            alignItems: "center",
            justifyContent: "center",
          }}
          accessibilityLabel="Đóng"
        >
          <Ionicons name="close" size={22} color={colors.onSurface} />
        </Pressable>

        <FlatList
          data={items}
          horizontal
          pagingEnabled
          initialScrollIndex={initialIndex}
          getItemLayout={(_, index) => ({
            length: SCREEN_WIDTH,
            offset: SCREEN_WIDTH * index,
            index,
          })}
          keyExtractor={(item, index) => item.mediaId || String(index)}
          showsHorizontalScrollIndicator={false}
          renderItem={({ item, index }) => {
            const uri = resolveDevMediaUrl(item.url || item.mediaUrl);
            return (
              <View
                style={{
                  width: SCREEN_WIDTH,
                  flex: 1,
                  alignItems: "center",
                  justifyContent: "center",
                  paddingHorizontal: 16,
                }}
              >
                {uri ? (
                  <Image
                    source={{ uri }}
                    style={{ width: "100%", height: "80%" }}
                    resizeMode="contain"
                  />
                ) : null}
                <Text style={{ marginTop: 12, fontSize: 14, color: "#FFFFFF" }}>
                  {index + 1} / {items.length}
                </Text>
              </View>
            );
          }}
        />
      </View>
    </Modal>
  );
}
