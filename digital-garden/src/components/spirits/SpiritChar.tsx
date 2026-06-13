import { forwardRef } from "react";
import type React from "react";
import type { SpiritConfig } from "./types";
import { PupilComp, EyeBallComp } from "./EyeComponents";

interface SpiritCharProps {
  config: SpiritConfig;
  isActive: boolean;
  transform: string;
  eyeTransform: string;
  haloColor?: string;
  lookX: number | undefined;
  lookY: number | undefined;
  isBlinking?: boolean;
  heightOverride?: number;
  onActivate: (event?: React.MouseEvent<HTMLDivElement>) => void;
  onKeyActivate: (event: React.KeyboardEvent<HTMLDivElement>) => void;
}

const SpiritChar = forwardRef<HTMLDivElement, SpiritCharProps>(function SpiritChar({
  config,
  isActive,
  transform,
  eyeTransform,
  haloColor,
  lookX,
  lookY,
  isBlinking = false,
  heightOverride,
  onActivate,
  onKeyActivate,
}, ref) {
  const { dim, info } = config;
  const bodyHeight = heightOverride ?? dim.h;

  const filter = isActive ? config.activeGlow : config.defaultShadow;

  return (
    <div
      ref={ref}
      role="button"
      tabIndex={0}
      aria-label={info.title}
      onClick={onActivate}
      onKeyDown={onKeyActivate}
      style={{
        position: "absolute",
        bottom: 0,
        left: dim.left,
        width: dim.w,
        zIndex: config.zIndex,
        cursor: "pointer",
        outline: "none",
        filter,
        transform,
        transformOrigin: "bottom center",
        transition:
          "transform 0.7s ease-in-out, filter 0.22s ease, opacity 0.22s ease",
      }}
    >
      {/* Halo */}
      <div
        style={{
          position: "absolute",
          inset: -9,
          border: `1px solid ${isActive ? info.color : "transparent"}`,
          borderRadius: 34,
          boxShadow: isActive
            ? `0 0 0 6px ${info.color}18, 0 0 34px ${info.color}55`
            : "none",
          opacity: isActive ? 1 : 0,
          transition:
            "opacity 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease",
          pointerEvents: "none",
        }}
      />

      {/* Body */}
      <div
        style={{
          width: "100%",
          height: bodyHeight,
          borderRadius: config.borderRadius,
          background: config.gradient,
          transition: "height 0.7s ease-in-out",
          boxShadow:
            config.id === "yellow"
              ? "inset 0 1px 0 rgba(255,255,255,0.15)"
              : config.id === "orange"
                ? "inset 0 1px 0 rgba(255,255,255,0.12)"
                : config.id === "purple"
                  ? "0 0 40px rgba(108,63,245,0.10), inset 0 1px 0 rgba(255,255,255,0.08)"
                  : "inset 0 1px 0 rgba(255,255,255,0.05)",
        }}
      />

      {/* Mouth (yellow only) */}
      {config.hasMouth && (
        <div
          style={{
            position: "absolute",
            bottom: (config.mouthBottomRatio ?? 0.15) * dim.h,
            left: "50%",
            width: config.mouthWidth ?? 26,
            height: config.mouthHeight ?? 11,
            borderRadius: "0 0 50% 50%",
            border: `2px solid ${config.mouthColor ?? "#8A7020"}`,
            borderTop: "none",
            transform: "translateX(-50%)",
          }}
        />
      )}

      {/* Eyes */}
      <div
        style={{
          position: "absolute",
          display: "flex",
          gap: config.eyeSize * config.eyeGapMultiplier,
          left: "50%",
          transform: eyeTransform,
          top: config.eyeOffsetTopRatio * dim.h,
          transition: config.eyeType === "eyeball" ? "all 0.7s ease-in-out" : "all 0.2s ease-out",
        }}
      >
        {config.eyeType === "eyeball"
          ? <>
              <EyeBallComp
                size={config.eyeSize}
                pupilSize={config.pupilSize}
                maxDistance={Math.round(config.eyeSize * 0.35)}
                isBlinking={isBlinking}
                forceLookX={lookX}
                forceLookY={lookY}
              />
              <EyeBallComp
                size={config.eyeSize}
                pupilSize={config.pupilSize}
                maxDistance={Math.round(config.eyeSize * 0.35)}
                isBlinking={isBlinking}
                forceLookX={lookX}
                forceLookY={lookY}
              />
            </>
          : <>
              <PupilComp
                size={config.pupilSize}
                maxDistance={Math.round(config.pupilSize * 0.6)}
                pupilColor={config.pupilColor}
                forceLookX={lookX}
                forceLookY={lookY}
              />
              <PupilComp
                size={config.pupilSize}
                maxDistance={Math.round(config.pupilSize * 0.6)}
                pupilColor={config.pupilColor}
                forceLookX={lookX}
                forceLookY={lookY}
              />
            </>
        }

        {/* Blush decorations */}
        <div
          style={{
            position: "absolute",
            left: -(config.eyeSize * 0.6),
            top: config.eyeSize * 0.5,
            width: config.eyeSize * 0.5,
            height: config.eyeSize * 0.28,
            borderRadius: "50%",
            background: "rgba(255,150,140,0.25)",
            filter: config.eyeType === "eyeball" ? "blur(2px)" : "blur(2.5px)",
            pointerEvents: "none",
          }}
        />
        <div
          style={{
            position: "absolute",
            right: -(config.eyeSize * 0.6),
            top: config.eyeSize * 0.5,
            width: config.eyeSize * 0.5,
            height: config.eyeSize * 0.28,
            borderRadius: "50%",
            background: "rgba(255,150,140,0.25)",
            filter: config.eyeType === "eyeball" ? "blur(2px)" : "blur(2.5px)",
            pointerEvents: "none",
          }}
        />
      </div>
    </div>
  );
});

export default SpiritChar;
