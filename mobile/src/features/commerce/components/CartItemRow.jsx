import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { formatVndPrice } from "../utils/formatVndPrice";
import { getLineTotal, getUnavailableLabel, isCartItemInvalid } from "../utils/cartDisplay";
import { CartQuantityStepper } from "./CartQuantityStepper";

function createStyles(colors) {
  return {
    card: {
      borderRadius: 12,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      padding: 16,
      gap: 12,
    },
    cardOutOfStock: {
      borderColor: colors.errorContainer,
      opacity: 0.95,
    },
    accentError: {
      position: "absolute",
      top: 0,
      left: 0,
      right: 0,
      height: 3,
      backgroundColor: colors.error,
      borderTopLeftRadius: 12,
      borderTopRightRadius: 12,
    },
    accentVacation: {
      position: "absolute",
      top: 0,
      left: 0,
      right: 0,
      height: 3,
      backgroundColor: colors.primary,
      borderTopLeftRadius: 12,
      borderTopRightRadius: 12,
    },
    topRow: {
      flexDirection: "row",
      alignItems: "flex-start",
      gap: 12,
    },
    checkbox: {
      width: 20,
      height: 20,
      borderRadius: 4,
      borderWidth: 1.5,
      borderColor: colors.outlineVariant,
      alignItems: "center",
      justifyContent: "center",
      marginTop: 4,
    },
    checkboxChecked: {
      backgroundColor: colors.primary,
      borderColor: colors.primary,
    },
    checkboxDisabled: {
      opacity: 0.4,
    },
    imageButton: {
      width: 96,
      height: 96,
      borderRadius: 12,
      overflow: "hidden",
      backgroundColor: colors.surfaceContainerLow,
    },
    image: {
      width: "100%",
      height: "100%",
    },
    imageGrayscale: {
      opacity: 0.65,
    },
    content: {
      flex: 1,
      gap: 8,
    },
    headerRow: {
      flexDirection: "row",
      alignItems: "flex-start",
      justifyContent: "space-between",
      gap: 8,
    },
    statusRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 4,
      marginBottom: 4,
    },
    statusText: {
      fontSize: 11,
      fontWeight: "700",
      textTransform: "uppercase",
    },
    statusError: {
      color: colors.error,
    },
    statusVacation: {
      color: colors.primary,
    },
    productName: {
      fontSize: 16,
      fontWeight: "600",
      color: colors.onSurface,
    },
    productNameInvalid: {
      textDecorationLine: "line-through",
      color: colors.onSurfaceVariant,
    },
    removeButton: {
      padding: 4,
    },
    vacationNote: {
      marginTop: 4,
      borderRadius: 8,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      padding: 10,
    },
    vacationText: {
      fontSize: 13,
      color: colors.onSurfaceVariant,
      lineHeight: 18,
    },
    footerRow: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      gap: 12,
      marginTop: 8,
    },
    lineTotal: {
      fontSize: 18,
      fontWeight: "600",
      color: colors.onSurface,
    },
    lineTotalInvalid: {
      color: colors.onSurfaceVariant,
      textDecorationLine: "line-through",
    },
  };
}

function CartCheckbox({ checked, disabled, onPress, accessibilityLabel, styles, colors }) {
  return (
    <Pressable
      style={[
        styles.checkbox,
        checked ? styles.checkboxChecked : null,
        disabled ? styles.checkboxDisabled : null,
      ]}
      disabled={disabled}
      onPress={onPress}
      accessibilityRole="checkbox"
      accessibilityState={{ checked, disabled }}
      accessibilityLabel={accessibilityLabel}
    >
      {checked ? <Ionicons name="checkmark" size={14} color={colors.onPrimary} /> : null}
    </Pressable>
  );
}

export function CartItemRow({
  item,
  isMutating = false,
  selected = false,
  canSelect = true,
  onToggleSelect,
  onOpenProduct,
  onRemove,
  onDecrease,
  onIncrease,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const invalid = isCartItemInvalid(item);
  const isOutOfStock = !item.inStock || item.unavailableReason === "OUT_OF_STOCK";
  const isVacation = item.unavailableReason === "SHOP_ON_VACATION";
  const unavailableLabel = item.validateMessage || getUnavailableLabel(item);
  const lineTotal = getLineTotal(item);
  const imageUri = resolveDevMediaUrl(item.imageUrl);

  return (
    <View
      style={[styles.card, isOutOfStock ? styles.cardOutOfStock : null]}
    >
      {isOutOfStock ? <View style={styles.accentError} /> : null}
      {isVacation ? <View style={styles.accentVacation} /> : null}

      <View style={styles.topRow}>
        <CartCheckbox
          checked={Boolean(selected)}
          disabled={!canSelect || isMutating}
          onPress={() => onToggleSelect?.(item.cartItemId)}
          accessibilityLabel={
            canSelect
              ? `Chọn ${item.productName}`
              : "Sản phẩm không thể chọn để thanh toán"
          }
          styles={styles}
          colors={colors}
        />

        <Pressable style={styles.imageButton} onPress={() => onOpenProduct?.(item.productId)}>
          {imageUri ? (
            <Image
              source={{ uri: imageUri }}
              style={[styles.image, isOutOfStock ? styles.imageGrayscale : null]}
              resizeMode="cover"
            />
          ) : (
            <View style={[styles.image, { backgroundColor: colors.surfaceContainerLow }]} />
          )}
        </Pressable>

        <View style={styles.content}>
          <View style={styles.headerRow}>
            <View style={{ flex: 1 }}>
              {invalid ? (
                <View style={styles.statusRow}>
                  <Ionicons
                    name={isOutOfStock ? "warning" : "airplane-outline"}
                    size={14}
                    color={isOutOfStock ? colors.error : colors.primary}
                  />
                  <Text
                    style={[
                      styles.statusText,
                      isOutOfStock ? styles.statusError : styles.statusVacation,
                    ]}
                  >
                    {unavailableLabel}
                  </Text>
                </View>
              ) : null}
              <Pressable onPress={() => onOpenProduct?.(item.productId)}>
                <Text
                  style={[styles.productName, invalid ? styles.productNameInvalid : null]}
                  numberOfLines={2}
                >
                  {item.productName}
                </Text>
              </Pressable>
            </View>
            <Pressable
              style={styles.removeButton}
              accessibilityLabel="Xóa sản phẩm"
              disabled={isMutating}
              onPress={() => onRemove?.(item.cartItemId)}
            >
              <Ionicons name="trash-outline" size={20} color={colors.onSurfaceVariant} />
            </Pressable>
          </View>

          {isVacation ? (
            <View style={styles.vacationNote}>
              <Text style={styles.vacationText}>
                Shop đang nghỉ — sản phẩm tạm thời không thể mua.
              </Text>
            </View>
          ) : null}

          <View style={styles.footerRow}>
            <CartQuantityStepper
              quantity={item.quantity}
              disabled={invalid}
              isLoading={isMutating}
              maxQuantity={item.availableQuantity}
              onDecrease={onDecrease}
              onIncrease={onIncrease}
            />
            <Text style={[styles.lineTotal, invalid ? styles.lineTotalInvalid : null]}>
              {formatVndPrice(lineTotal)}
            </Text>
          </View>
        </View>
      </View>
    </View>
  );
}
