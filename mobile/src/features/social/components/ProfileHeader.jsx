import { useState } from "react";
import {
  ActivityIndicator,
  Image,
  ImageBackground,
  Linking,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import { COVER_IMAGE_URL } from "../constants/profileConstants";
import { ROUTES } from "../../../shared/constants/routes";
import { resolveDevMediaUrl } from "../../../shared/utils/resolveDevMediaUrl";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { FollowButton } from "./FollowButton";
import { ProfileImageLightbox } from "./ProfileImageLightbox";
import { ProfileStats } from "./ProfileStats";

const DEFAULT_AVATAR = "https://i.pravatar.cc/200?img=11";

function createStyles(colors) {
  return {
    root: {
      backgroundColor: colors.surfaceContainerLowest,
    },
    coverPressable: {
      height: 140,
      width: "100%",
    },
    cover: {
      height: 140,
      width: "100%",
    },
    coverOverlay: {
      ...StyleSheet.absoluteFillObject,
      backgroundColor: "rgba(0,0,0,0.15)",
    },
    body: {
      alignItems: "center",
      paddingHorizontal: 16,
      paddingBottom: 16,
      marginTop: -48,
    },
    avatarWrap: {
      borderRadius: 64,
      borderWidth: 4,
      borderColor: colors.surfaceContainerLowest,
      overflow: "hidden",
      backgroundColor: colors.surfaceContainerHigh,
    },
    avatar: {
      width: 96,
      height: 96,
    },
    nameRow: {
      flexDirection: "row",
      alignItems: "center",
      gap: 6,
      marginTop: 12,
    },
    displayName: {
      fontSize: 22,
      fontWeight: "600",
      color: colors.onSurface,
      textAlign: "center",
    },
    username: {
      marginTop: 4,
      fontSize: 14,
      color: colors.onSurfaceVariant,
    },
    privateNotice: {
      marginTop: 8,
      fontSize: 14,
      color: colors.onSurfaceVariant,
      textAlign: "center",
    },
    detailsLoader: {
      marginTop: 12,
    },
    detailsErrorWrap: {
      marginTop: 8,
      alignItems: "center",
      gap: 6,
    },
    detailsError: {
      fontSize: 13,
      color: colors.onErrorContainer,
      textAlign: "center",
    },
    retryLink: {
      fontSize: 13,
      fontWeight: "600",
      color: colors.primary,
    },
    bio: {
      marginTop: 8,
      fontSize: 14,
      lineHeight: 20,
      color: colors.onSurfaceVariant,
      textAlign: "center",
      maxWidth: 320,
    },
    website: {
      marginTop: 6,
      fontSize: 14,
      color: colors.primary,
    },
    socialLinks: {
      marginTop: 8,
      flexDirection: "row",
      flexWrap: "wrap",
      justifyContent: "center",
      gap: 12,
    },
    socialLink: {
      fontSize: 14,
      color: colors.primary,
      textTransform: "capitalize",
    },
    actions: {
      marginTop: 16,
      alignItems: "center",
      gap: 10,
    },
    editButton: {
      minHeight: 44,
      minWidth: 168,
      borderRadius: 8,
      backgroundColor: colors.primary,
      alignItems: "center",
      justifyContent: "center",
      paddingHorizontal: 20,
      paddingVertical: 10,
    },
    editButtonPressed: {
      opacity: 0.92,
    },
    editButtonText: {
      fontSize: 14,
      fontWeight: "600",
      color: colors.onPrimary,
    },
    shopButton: {
      minHeight: 44,
      minWidth: 168,
      borderRadius: 8,
      borderWidth: 2,
      borderColor: colors.outlineVariant,
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "center",
      gap: 8,
      paddingHorizontal: 20,
      paddingVertical: 10,
      backgroundColor: colors.surfaceContainerLowest,
    },
    shopButtonPressed: {
      borderColor: colors.primary,
    },
    shopButtonText: {
      fontSize: 14,
      fontWeight: "500",
      color: colors.onSurface,
    },
  };
}

export function ProfileHeader({
  profile,
  details,
  coverImageUrl,
  commerceShop,
  isDetailsLoading = false,
  detailsError = "",
  onDetailsRetry,
  onFollowPress,
  isFollowLoading = false,
  followDisabled = false,
  followDisabledTitle,
  onFollowersPress,
  onFollowingPress,
  onEditProfilePress,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const [imagePreview, setImagePreview] = useState(null);

  if (!profile) return null;

  const isSelf = profile.followStatus === "SELF";
  const isPrivateAccount = Boolean(profile.isPrivate);
  const showFollowButton = !isSelf && !isPrivateAccount;
  const showDetails = !details?.showPrivateNotice && !isDetailsLoading && !detailsError;

  const resolvedCoverUrl =
    String(coverImageUrl || profile.coverUrl || profile.cover_url || "").trim() ||
    COVER_IMAGE_URL;
  const avatarUrl = resolveDevMediaUrl(profile.avatarUrl || DEFAULT_AVATAR);

  const openShop = () => {
    if (!commerceShop?.shopId) return;
    router.push(ROUTES.commerceShopProducts(commerceShop.shopId));
  };

  return (
    <View style={styles.root}>
      <Pressable
        onPress={() => setImagePreview("cover")}
        accessibilityRole="button"
        accessibilityLabel="Xem ảnh bìa"
      >
        <ImageBackground source={{ uri: resolvedCoverUrl }} style={styles.cover} resizeMode="cover">
          <View style={styles.coverOverlay} />
        </ImageBackground>
      </Pressable>

      <View style={styles.body}>
        <Pressable
          style={styles.avatarWrap}
          onPress={() => setImagePreview("avatar")}
          accessibilityRole="button"
          accessibilityLabel="Xem ảnh đại diện"
        >
          <Image source={{ uri: avatarUrl }} style={styles.avatar} />
        </Pressable>

        <View style={styles.nameRow}>
          <Text style={styles.displayName}>{profile.displayName}</Text>
          {profile.isPrivate ? (
            <Ionicons name="lock-closed" size={18} color={colors.onSurfaceVariant} />
          ) : (
            <Ionicons name="checkmark-circle" size={18} color={colors.primary} />
          )}
        </View>

        {showDetails && details?.username ? (
          <Text style={styles.username}>@{details.username}</Text>
        ) : null}

        {details?.showPrivateNotice ? (
          <Text style={styles.privateNotice}>Tài khoản đang ở chế độ riêng tư.</Text>
        ) : null}

        {isDetailsLoading ? (
          <ActivityIndicator style={styles.detailsLoader} color={colors.primary} />
        ) : null}

        {detailsError ? (
          <View style={styles.detailsErrorWrap}>
            <Text style={styles.detailsError}>{detailsError}</Text>
            {onDetailsRetry ? (
              <Pressable onPress={onDetailsRetry} accessibilityRole="button">
                <Text style={styles.retryLink}>Thử lại</Text>
              </Pressable>
            ) : null}
          </View>
        ) : null}

        {showDetails && details?.bio ? (
          <Text style={styles.bio}>{details.bio}</Text>
        ) : null}

        {showDetails && details?.website ? (
          <Pressable
            onPress={() => Linking.openURL(details.website)}
            accessibilityRole="link"
          >
            <Text style={styles.website}>{details.website}</Text>
          </Pressable>
        ) : null}

        {showDetails && details?.socialLinks ? (
          <View style={styles.socialLinks}>
            {Object.entries(details.socialLinks)
              .filter(([, url]) => String(url || "").trim())
              .map(([platform, url]) => (
                <Pressable
                  key={platform}
                  onPress={() => Linking.openURL(url)}
                  accessibilityRole="link"
                >
                  <Text style={styles.socialLink}>{platform}</Text>
                </Pressable>
              ))}
          </View>
        ) : null}

        <ProfileStats
          followerCount={profile.followerCount}
          followingCount={profile.followingCount}
          onFollowersPress={onFollowersPress}
          onFollowingPress={onFollowingPress}
        />

        <View style={styles.actions}>
          {commerceShop?.hasShop && commerceShop.shopId ? (
            <Pressable
              style={({ pressed }) => [styles.shopButton, pressed && styles.shopButtonPressed]}
              onPress={openShop}
              accessibilityRole="button"
              accessibilityLabel={
                commerceShop.shopName
                  ? `Shop: ${commerceShop.shopName}`
                  : "Xem shop"
              }
            >
              <Ionicons name="storefront-outline" size={18} color={colors.onSurface} />
              <Text style={styles.shopButtonText} numberOfLines={1}>
                {commerceShop.shopName ? `Shop: ${commerceShop.shopName}` : "Xem shop"}
              </Text>
            </Pressable>
          ) : null}

          {isSelf ? (
            <Pressable
              style={({ pressed }) => [styles.editButton, pressed && styles.editButtonPressed]}
              onPress={onEditProfilePress}
              accessibilityRole="button"
              accessibilityLabel="Chỉnh sửa hồ sơ"
            >
              <Text style={styles.editButtonText}>Chỉnh sửa hồ sơ</Text>
            </Pressable>
          ) : showFollowButton ? (
            <FollowButton
              followStatus={profile.followStatus}
              onPress={onFollowPress}
              isLoading={isFollowLoading}
              disabled={followDisabled}
              disabledTitle={followDisabledTitle}
            />
          ) : null}
        </View>
      </View>

      {imagePreview === "avatar" ? (
        <ProfileImageLightbox
          imageUrl={avatarUrl}
          label="Ảnh đại diện"
          onClose={() => setImagePreview(null)}
        />
      ) : null}

      {imagePreview === "cover" ? (
        <ProfileImageLightbox
          imageUrl={resolvedCoverUrl}
          label="Ảnh bìa"
          onClose={() => setImagePreview(null)}
        />
      ) : null}
    </View>
  );
}
