import { useMemo } from "react";
import { mapProductTagsFromApi } from "../utils/mapProductTagsFromApi";

export function useEnrichedProductTags(tags) {
  return useMemo(() => mapProductTagsFromApi(tags), [tags]);
}
