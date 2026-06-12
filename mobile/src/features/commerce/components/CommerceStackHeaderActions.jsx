import { Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ROUTES } from "../../../shared/constants/routes";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { useCartBadgeCount } from "../hooks/useCartBadgeCount";

function createStyles(colors) {
  return {
    row: {
      flexDirection: "row",
      justifyContent: "flex-end",
      gap: 4,
      paddingHorizontal: 4,
    },
    btn: {
      width: 40,
      height: 40,
      borderRadius: 20,
      alignItems: "center",
      justifyContent: "center",
    },
    cartWrap: {
      position: "relative",
    },
    badge: {
      position: "absolute",
      top: 2,
      right: 2,
      minWidth: 18,
      height: 18,
      borderRadius: 9,
      paddingHorizontal: 4,
      alignItems: "center",
      justifyContent: "center",
      backgroundColor: colors.primary,
    },
    badgeText: {
      fontSize: 10,
      fontWeight: "700",
      color: colors.onPrimary,
    },
  };
}

export function CommerceStackHeaderActions() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const iconColor = colors.onSurfaceVariant;
  const badgeCount = useCartBadgeCount();
  const badgeLabel = badgeCount > 99 ? "99+" : String(badgeCount);

  return (
    <View style={styles.row}>
      <Pressable
        style={styles.btn}
        onPress={() => router.push(ROUTES.commerceSearch)}
        accessibilityLabel="Tìm kiếm sản phẩm"
      >
        <Ionicons name="search-outline" size={22} color={iconColor} />
      </Pressable>
      <Pressable
        style={styles.btn}
        onPress={() => router.push(ROUTES.commerceOrders)}
        accessibilityLabel="Đơn hàng của tôi"
      >
        <Ionicons name="receipt-outline" size={22} color={iconColor} />
      </Pressable>
      <Pressable
        style={[styles.btn, styles.cartWrap]}
        onPress={() => router.push(ROUTES.commerceCart)}
        accessibilityLabel={
          badgeCount > 0 ? `Giỏ hàng, ${badgeCount} sản phẩm` : "Giỏ hàng"
        }
      >
        <Ionicons name="bag-outline" size={22} color={iconColor} />
        {badgeCount > 0 ? (
          <View style={styles.badge}>
            <Text style={styles.badgeText}>{badgeLabel}</Text>
          </View>
        ) : null}
      </Pressable>
    </View>
  );
}