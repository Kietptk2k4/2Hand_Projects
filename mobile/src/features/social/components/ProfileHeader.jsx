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
import { COVER_IMAGE_URL } from "../constants/profileConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";
import { FollowButton } from "./FollowButton";
import { ProfileStats } from "./ProfileStats";

const DEFAULT_AVATAR = "https://i.pravatar.cc/200?img=11";

function createStyles(colors) {
  return {
    root: {
      backgroundColor: colors.surfaceContainerLowest,
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
    detailsError: {
      marginTop: 8,
      fontSize: 13,
      color: colors.onErrorContainer,
      textAlign: "center",
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
  };
}

export function ProfileHeader({
  profile,
  details,
  isDetailsLoading = false,
  detailsError = "",
  postCount = null,
  onFollowPress,
  isFollowLoading = false,
  onFollowersPress,
  onFollowingPress,
  onEditProfilePress,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);

  if (!profile) return null;

  const isSelf = profile.followStatus === "SELF";
  const showDetails = !details?.showPrivateNotice && !isDetailsLoading && !detailsError;

  return (
    <View style={styles.root}>
      <ImageBackground source={{ uri: COVER_IMAGE_URL }} style={styles.cover} resizeMode="cover">
        <View style={styles.coverOverlay} />
      </ImageBackground>

      <View style={styles.body}>
        <View style={styles.avatarWrap}>
          <Image
            source={{ uri: profile.avatarUrl || DEFAULT_AVATAR }}
            style={styles.avatar}
          />
        </View>

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
          <Text style={styles.detailsError}>{detailsError}</Text>
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
          postCount={postCount}
          followerCount={profile.followerCount}
          followingCount={profile.followingCount}
          onFollowersPress={onFollowersPress}
          onFollowingPress={onFollowingPress}
        />

        <View style={styles.actions}>
          {isSelf ? (
            <Pressable
              style={({ pressed }) => [styles.editButton, pressed && styles.editButtonPressed]}
              onPress={onEditProfilePress}
              accessibilityRole="button"
              accessibilityLabel="Chỉnh sửa hồ sơ"
            >
              <Text style={styles.editButtonText}>Chỉnh sửa hồ sơ</Text>
            </Pressable>
          ) : (
            <FollowButton
              followStatus={profile.followStatus}
              onPress={onFollowPress}
              isLoading={isFollowLoading}
            />
          )}
        </View>
      </View>
    </View>
  );
}
