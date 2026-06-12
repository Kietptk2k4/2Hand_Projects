import { Tabs } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { AccountSettingsHeaderButton } from "../../src/features/auth/account/components/AccountSettingsHeaderButton";
import { CommerceStackHeaderActions } from "../../src/features/commerce/components/CommerceStackHeaderActions";
import { useThemeColors } from "../../src/shared/theme/useThemeColors";

export default function TabsLayout() {
  const colors = useThemeColors();

  return (
    <Tabs
      screenOptions={{
        headerStyle: { backgroundColor: colors.surface },
        headerTintColor: colors.onSurface,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.onSurfaceVariant,
        tabBarStyle: {
          backgroundColor: colors.surfaceContainerLowest,
          borderTopColor: colors.outlineVariant,
        },
      }}
    >
      <Tabs.Screen
        name="feed"
        options={{
          title: "Feed",
          tabBarLabel: "Trang chủ",
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="home-outline" size={size} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="shop"
        options={{
          title: "Cửa hàng",
          tabBarLabel: "Cửa hàng",
          headerRight: () => <CommerceStackHeaderActions />,
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="bag-outline" size={size} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: "Hồ sơ",
          tabBarLabel: "Hồ sơ",
          headerRight: () => <AccountSettingsHeaderButton />,
          tabBarIcon: ({ color, size }) => (
            <Ionicons name="person-outline" size={size} color={color} />
          ),
        }}
      />
    </Tabs>
  );
}
