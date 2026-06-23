export default {
  expo: {
    name: "2Hands",
    slug: "mobile",
    version: "1.0.0",
    orientation: "portrait",
    scheme: "twohands",
    userInterfaceStyle: "automatic",
    plugins: ["expo-video"],
    android: {
      usesCleartextTraffic: true,
    },
    extra: {
      devHost: process.env.EXPO_PUBLIC_DEV_HOST,
      authServiceBaseUrl: process.env.EXPO_PUBLIC_AUTH_SERVICE_BASE_URL,
    },
  },
};
