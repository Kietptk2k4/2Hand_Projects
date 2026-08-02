"""PopularityNormalizer for Commerce Home (train-fit z_lo/z_hi, not pool min-max)."""

from __future__ import annotations

import math
from dataclasses import dataclass


@dataclass(frozen=True)
class PopularityNormalizer:
    z_lo: float
    z_hi: float
    eps: float = 1e-9

    def normalize(self, z: float) -> float:
        denom = (self.z_hi - self.z_lo) + self.eps
        score = (z - self.z_lo) / denom
        return max(0.0, min(1.0, score))

    @staticmethod
    def log1p_raw(raw: int | float) -> float:
        return math.log1p(max(0.0, float(raw)))

    @classmethod
    def fit_from_raw(cls, raw_values: list[int | float], lo_pct: float = 1.0, hi_pct: float = 99.0) -> PopularityNormalizer:
        """Fit on train raw completed_order_item counts via log1p then percentiles."""
        if not raw_values:
            return cls(z_lo=0.0, z_hi=1.0)
        zs = sorted(cls.log1p_raw(r) for r in raw_values)
        n = len(zs)

        def pct(p: float) -> float:
            if n == 1:
                return zs[0]
            idx = (p / 100.0) * (n - 1)
            lo = int(math.floor(idx))
            hi = int(math.ceil(idx))
            if lo == hi:
                return zs[lo]
            w = idx - lo
            return zs[lo] * (1 - w) + zs[hi] * w

        z_lo = pct(lo_pct)
        z_hi = pct(hi_pct)
        if z_hi <= z_lo:
            z_hi = z_lo + 1.0
        return cls(z_lo=z_lo, z_hi=z_hi)

    def to_dict(self) -> dict[str, float]:
        return {"z_lo": self.z_lo, "z_hi": self.z_hi}

    @classmethod
    def from_dict(cls, data: dict[str, float]) -> PopularityNormalizer:
        return cls(z_lo=float(data["z_lo"]), z_hi=float(data["z_hi"]))
