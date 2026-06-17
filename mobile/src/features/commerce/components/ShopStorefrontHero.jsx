import { useState } from "react";
import { Image, Pressable, Text, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { ProfileImageLightbox } from "../../social/components/ProfileImageLightbox";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    card: {
      marginBottom: 16,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLowest,
      overflow: "hidden",
    },
    cover: {
      height: 160,
      backgroundColor: colors.surfaceContainerHigh,
    },
    coverImage: {
      width: "100%",
      height: "100%",
    },
    coverPlaceholder: {
      flex: 1,
      backgroundColor: colors.surfaceContainerHigh,
    },
    body: {
      alignItems: "center",
      paddingHorizontal: 16,
      paddingBottom: 20,
    },
    avatarWrap: {
      marginTop: -48,
      width: 96,
      height: 96,
      borderRadius: 48,
      borderWidth: 4,
      borderColor: colors.surfaceContainerLowest,
      backgroundColor: colors.surfaceContainerLow,
      overflow: "hidden",
      alignItems: "center",
      justifyContent: "center",
    },
    avatar: {
      width: "100%",
      height: "100%",
    },
    shopName: {
      marginTop: 12,
      fontSize: 22,
      fontWeight: "700",
      color: colors.onSurface,
      textAlign: "center",
    },
    ratingRow: {
      marginTop: 8,
      flexDirection: "row",
      alignItems: "center",
      gap: 4,
    },
    ratingText: {
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
    ratingValue: {
      fontWeight: "600",
      color: colors.onSurface,
    },
    description: {
      marginTop: 12,
      fontSize: 15,
      lineHeight: 22,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    profileLink: {
      marginTop: 12,
      flexDirection: "row",
      alignItems: "center",
      gap: 6,
    },
    profileLinkText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.primary,
    },
    reviewsLink: {
      marginTop: 8,
      paddingVertical: 4,
    },
    reviewsLinkText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.primary,
    },
  };
}

export function ShopStorefrontHero({ shop, onViewReviews }) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [imagePreview, setImagePreview] = useState(null);

  if (!shop) return null;

  const coverUri = resolveDevMediaUrl(shop.coverUrl);
  const avatarUri = resolveDevMediaUrl(shop.avatarUrl);

  return (
    <View style={styles.card}>
      <Pressable
        onPress={() => coverUri && setImagePreview("cover")}
        disabled={!coverUri}
        accessibilityRole="button"
        accessibilityLabel="Xem hình nền shop"
      >
        {coverUri ? (
          <Image source={{ uri: coverUri }} style={[styles.cover, styles.coverImage]} resizeMode="cover" />
        ) : (
          <View style={styles.cover} />
        )}
      </Pressable>

      <View style={styles.body}>
        <Pressable
          onPress={() => avatarUri && setImagePreview("avatar")}
          disabled={!avatarUri}
          accessibilityRole="button"
          accessibilityLabel="Xem avatar shop"
        >
          <View style={styles.avatarWrap}>
            {avatarUri ? (
              <Image source={{ uri: avatarUri }} style={styles.avatar} resizeMode="cover" />
            ) : (
              <Ionicons name="storefront-outline" size={36} color={colors.outline} />
            )}
          </View>
        </Pressable>

        <Text style={styles.shopName}>{shop.shopName}</Text>

        {shop.ratingCount > 0 ? (
          <View style={styles.ratingRow}>
            <Ionicons name="star" size={16} color="#F59E0B" />
            <Text style={styles.ratingText}>
              <Text style={styles.ratingValue}>{shop.ratingAvg}</Text>
              {` · ${shop.ratingCount} đánh giá`}
            </Text>
          </View>
        ) : null}

        {shop.description ? (
          <Text style={styles.description} numberOfLines={4}>
            {shop.description}
          </Text>
        ) : null}

        {shop.sellerId ? (
          <Pressable
            style={styles.profileLink}
            onPress={() => router.push(ROUTES.userProfile(shop.sellerId))}
          >
            <Ionicons name="person-outline" size={18} color={colors.primary} />
            <Text style={styles.profileLinkText}>Xem hồ sơ chủ shop</Text>
          </Pressable>
        ) : null}

        {shop.ratingCount > 0 && onViewReviews ? (
          <Pressable style={styles.reviewsLink} onPress={onViewReviews}>
            <Text style={styles.reviewsLinkText}>Xem tất cả đánh giá của shop</Text>
          </Pressable>
        ) : null}
      </View>

      {imagePreview === "avatar" && avatarUri ? (
        <ProfileImageLightbox
          imageUrl={avatarUri}
          label="Avatar shop"
          onClose={() => setImagePreview(null)}
        />
      ) : null}

      {imagePreview === "cover" && coverUri ? (
        <ProfileImageLightbox
          imageUrl={coverUri}
          label="Hình nền shop"
          onClose={() => setImagePreview(null)}
        />
      ) : null}
    </View>
  );
}

export function ShopStorefrontHeroSkeleton() {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  return (
    <View style={[styles.card, { opacity: 0.7 }]}>
      <View style={styles.cover} />
      <View style={styles.body}>
        <View style={[styles.avatarWrap, { backgroundColor: colors.surfaceContainerHigh }]} />
        <View
          style={{
            marginTop: 12,
            width: 180,
            height: 22,
            borderRadius: 6,
            backgroundColor: colors.surfaceContainerHigh,
          }}
        />
      </View>
    </View>
  );
}
