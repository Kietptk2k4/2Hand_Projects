import * as WebBrowser from "expo-web-browser";

export async function openPayOsBrowser(payosUrl) {
  if (!payosUrl) return null;
  return WebBrowser.openBrowserAsync(payosUrl, {
    showInRecents: true,
    createTask: false,
  });
}