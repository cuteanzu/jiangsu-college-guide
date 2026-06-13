export type SpiritId = "purple" | "black" | "orange" | "yellow";

export type EyeType = "eyeball" | "pupil";

export interface SpiritDimensions {
  w: number;
  h: number;
  hTall?: number; // taller variant for purple when typing
  left: number;
}

export interface SpiritGuideInfo {
  name: string;
  title: string;
  description: string;
  color: string;
}

export interface SpiritConfig {
  id: SpiritId;
  dim: SpiritDimensions;
  zIndex: number;
  gradient: string;
  borderRadius: string;
  eyeType: EyeType;
  eyeSize: number;
  pupilSize: number;
  pupilColor: string;
  eyeOffsetTopRatio: number;
  eyeGapMultiplier: number;
  activeGlow: string;
  defaultShadow: string;
  hasMouth?: boolean;
  mouthBottomRatio?: number;
  mouthWidth?: number;
  mouthHeight?: number;
  mouthColor?: string;
  charIdx: number; // for idleBreath variation
  info: SpiritGuideInfo;
}
