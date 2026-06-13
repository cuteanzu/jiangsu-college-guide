import { useEffect, useRef, useState } from "react";

export interface PupilCompProps {
  size?: number;
  maxDistance?: number;
  pupilColor?: string;
  forceLookX?: number;
  forceLookY?: number;
}

export function PupilComp({ size = 12, maxDistance = 5, pupilColor = "#2D2D2D", forceLookX, forceLookY }: PupilCompProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState({ x: 0, y: 0 });

  useEffect(() => {
    const onMove = (e: MouseEvent) => {
      if (!ref.current) return;
      if (forceLookX !== undefined && forceLookY !== undefined) {
        setPos({ x: forceLookX, y: forceLookY });
        return;
      }
      const r = ref.current.getBoundingClientRect();
      const cx = r.left + r.width / 2;
      const cy = r.top + r.height / 2;
      const dx = e.clientX - cx;
      const dy = e.clientY - cy;
      const d = Math.min(Math.sqrt(dx * dx + dy * dy), maxDistance);
      const a = Math.atan2(dy, dx);
      setPos({ x: Math.cos(a) * d, y: Math.sin(a) * d });
    };
    window.addEventListener("mousemove", onMove, { passive: true });
    return () => window.removeEventListener("mousemove", onMove);
  }, [maxDistance, forceLookX, forceLookY]);

  const sparkleSize = Math.max(1.5, Math.round(size * 0.18));

  return (
    <div
      ref={ref}
      style={{
        width: size, height: size,
        borderRadius: "50%",
        backgroundColor: pupilColor,
        transform: "translate(" + pos.x.toFixed(1) + "px," + pos.y.toFixed(1) + "px)",
        transition: "transform 0.08s ease-out",
        position: "relative",
      }}
    >
      <div style={{
        position: "absolute",
        top: Math.round(size * 0.1),
        right: Math.round(size * 0.14),
        width: sparkleSize,
        height: sparkleSize,
        borderRadius: "50%",
        backgroundColor: "rgba(255,255,255,0.8)",
      }} />
    </div>
  );
}

export interface EyeBallCompProps {
  size?: number;
  pupilSize?: number;
  maxDistance?: number;
  eyeColor?: string;
  pupilColor?: string;
  isBlinking?: boolean;
  forceLookX?: number;
  forceLookY?: number;
}

export function EyeBallComp({ size = 48, pupilSize = 16, maxDistance = 10, eyeColor = "white", pupilColor = "#2D2D2D", isBlinking = false, forceLookX, forceLookY }: EyeBallCompProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState({ x: 0, y: 0 });

  useEffect(() => {
    const onMove = (e: MouseEvent) => {
      if (!ref.current) return;
      if (forceLookX !== undefined && forceLookY !== undefined) {
        setPos({ x: forceLookX, y: forceLookY });
        return;
      }
      const r = ref.current.getBoundingClientRect();
      const cx = r.left + r.width / 2;
      const cy = r.top + r.height / 2;
      const dx = e.clientX - cx;
      const dy = e.clientY - cy;
      const d = Math.min(Math.sqrt(dx * dx + dy * dy), maxDistance);
      const a = Math.atan2(dy, dx);
      setPos({ x: Math.cos(a) * d, y: Math.sin(a) * d });
    };
    window.addEventListener("mousemove", onMove, { passive: true });
    return () => window.removeEventListener("mousemove", onMove);
  }, [maxDistance, forceLookX, forceLookY]);

  const sparkleSize = Math.max(2, Math.round(pupilSize * 0.22));

  return (
    <div
      ref={ref}
      style={{
        width: size, height: size,
        borderRadius: "50%",
        backgroundColor: eyeColor,
        overflow: "hidden",
        display: "flex", alignItems: "center", justifyContent: "center",
        border: "1px solid " + pupilColor + "22",
        boxShadow: "inset 0 1px 2px " + pupilColor + "18",
        animation: isBlinking ? "blinkAnim 0.18s ease-in-out" : "none",
      }}
    >
      <div
        style={{
          width: pupilSize, height: pupilSize,
          borderRadius: "50%",
          backgroundColor: pupilColor,
          transform: "translate(" + pos.x.toFixed(1) + "px," + pos.y.toFixed(1) + "px)",
          transition: "transform 0.08s ease-out",
          position: "relative",
        }}
      >
        <div style={{
          position: "absolute",
          top: Math.round(pupilSize * 0.12),
          right: Math.round(pupilSize * 0.15),
          width: sparkleSize,
          height: sparkleSize,
          borderRadius: "50%",
          backgroundColor: "rgba(255,255,255,0.85)",
        }} />
      </div>
    </div>
  );
}
