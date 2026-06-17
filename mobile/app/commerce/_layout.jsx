import { Stack } from "expo-router";
import { CommerceStackHeaderActions } from "../../src/features/commerce/components/CommerceStackHeaderActions";
import { useThemeColors } from "../../src/shared/theme/useThemeColors";

export default function CommerceLayout() {
  const colors = useThemeColors();

  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: colors.surface },
        headerTintColor: colors.onSurface,
        headerTitleStyle: { fontWeight: "600" },
        contentStyle: { backgroundColor: colors.surface },
        headerRight: () => <CommerceStackHeaderActions />,
      }}
    >
      <Stack.Screen name="search" options={{ title: "Tìm kiếm" }} />
      <Stack.Screen name="cart" options={{ title: "Giỏ hàng" }} />
      <Stack.Screen name="addresses/index" options={{ title: "Địa chỉ giao hàng" }} />
      <Stack.Screen name="addresses/create" options={{ title: "Thêm địa chỉ" }} />
      <Stack.Screen name="addresses/[addressId]" options={{ title: "Sửa địa chỉ" }} />
      <Stack.Screen name="categories/[categoryId]" options={{ title: "Sản phẩm danh mục" }} />
      <Stack.Screen name="products/[productId]/index" options={{ title: "Chi tiết sản phẩm" }} />
      <Stack.Screen name="products/[productId]/reviews" options={{ title: "Đánh giá sản phẩm" }} />
      <Stack.Screen name="shops/[shopId]/index" options={{ title: "Cửa hàng" }} />
      <Stack.Screen name="shops/[shopId]/reviews" options={{ title: "Đánh giá cửa hàng" }} />
      <Stack.Screen name="checkout/index" options={{ title: "Thanh toán" }} />
      <Stack.Screen name="checkout/payment-result" options={{ title: "Kết quả thanh toán" }} />
      <Stack.Screen name="checkout/vnpay-return" options={{ title: "Kết quả VNPay" }} />
      <Stack.Screen name="checkout/success" options={{ title: "Đặt hàng thành công" }} />
      <Stack.Screen name="orders/index" options={{ title: "Đơn hàng" }} />
      <Stack.Screen name="orders/[orderId]/index" options={{ title: "Chi tiết đơn hàng" }} />
      <Stack.Screen name="orders/[orderId]/shipments/[shipmentId]" options={{ title: "Theo dõi vận chuyển" }} />
      <Stack.Screen name="reviews/new" options={{ title: "Viết đánh giá" }} />
      <Stack.Screen name="reviews/[reviewId]/edit" options={{ title: "Sửa đánh giá" }} />
    </Stack>
  );
}
