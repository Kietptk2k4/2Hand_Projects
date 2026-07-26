"""Deterministic UUID / id helpers for simulation seed."""

from __future__ import annotations


def user_uuid(index: int) -> str:
    return f"a1000000-0000-4000-8000-{index:012d}"


def shop_uuid(index: int) -> str:
    return f"b2000000-0000-4000-8000-{index:012d}"


def product_uuid(index: int) -> str:
    return f"c3000000-0000-4000-8000-{index:012d}"


def cart_uuid(index: int) -> str:
    return f"d4000000-0000-4000-8000-{index:012d}"


def order_uuid(index: int) -> str:
    return f"e5000000-0000-4000-8000-{index:012d}"


def post_id(index: int) -> str:
    # 24-char hex-like Mongo ObjectId stand-in
    return f"simpost{index:016d}"
