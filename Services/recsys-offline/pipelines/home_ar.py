"""Apriori association rules: interest_tag → commerce_category (D14)."""

from __future__ import annotations

from collections import defaultdict
from dataclasses import dataclass
from itertools import combinations
from typing import Iterable


@dataclass(frozen=True)
class Basket:
    user_id: str
    tags: frozenset[tuple[str, str]]  # (tag_type, tag)
    categories: frozenset[str]


def mine_tag_category_rules(
    baskets: Iterable[Basket],
    min_support: float = 0.01,
    min_confidence: float = 0.05,
) -> list[dict]:
    basket_list = [b for b in baskets if b.tags or b.categories]
    n = len(basket_list)
    if n == 0:
        return []

    tag_count: dict[tuple[str, str], int] = defaultdict(int)
    pair_count: dict[tuple[tuple[str, str], str], int] = defaultdict(int)

    for basket in basket_list:
        for tag in basket.tags:
            tag_count[tag] += 1
            for cat in basket.categories:
                pair_count[(tag, cat)] += 1

    rules: list[dict] = []
    for (tag, cat), cnt in pair_count.items():
        support = cnt / n
        if support < min_support:
            continue
        tag_sup = tag_count[tag] / n
        if tag_sup <= 0:
            continue
        confidence = (cnt / n) / tag_sup
        if confidence < min_confidence:
            continue
        rules.append(
            {
                "tag_type": tag[0],
                "tag": tag[1],
                "category_id": cat,
                "support": support,
                "confidence": confidence,
            }
        )
    return rules
