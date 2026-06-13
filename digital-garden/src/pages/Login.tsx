import { useState, useEffect, useRef, useCallback, Component } from "react";
import { useNavigate } from "react-router-dom";
import styled, { keyframes, createGlobalStyle } from "styled-components";
import { Eye, EyeOff } from "lucide-react";
import { createSeasonalFX } from "seasonalfx";
import type { ISeasonalFX } from "seasonalfx";
import { useSettings } from "../settings-context";
import api from "../services/api";
import Bokeh from "../Bokeh";
import Komorebi from "../Komorebi";
import GroundPetals from "../GroundPetals";
import DeskPet from "../DeskPet";
import bgImage from "/bg.webp";

// ── Petal texture ──

function createPetalImage(): string {
  const size = 48;
  const c = document.createElement("canvas");
  c.width = c.height = size;
  const ctx = c.getContext("2d")!;
  const h = size / 2;
  ctx.save();
  ctx.translate(h, h);
  // Plum blossom petal — broad oval, no notch
  const grad = ctx.createRadialGradient(0, -h * 0.1, h * 0.03, 0, h * 0.05, h * 0.9);
  grad.addColorStop(0, "rgba(255,242,245,1)");
  grad.addColorStop(0.15, "rgba(255,220,230,0.95)");
  grad.addColorStop(0.45, "rgba(245,190,205,0.7)");
  grad.addColorStop(0.75, "rgba(235,155,175,0.3)");
  grad.addColorStop(1, "rgba(220,125,150,0)");
  ctx.fillStyle = grad;
  ctx.beginPath();
  ctx.ellipse(0, -h * 0.12, h * 0.52, h * 0.72, 0, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
  return c.toDataURL("image/png");
}

const petalImg = createPetalImage();

// ── Animations ──

import { fadeUp, shakeAnim, popIn } from "../components/animations";

const InlineKeyframes = createGlobalStyle`
  @keyframes blinkAnim {
    0%, 100% { transform: scaleY(1); }
    30% { transform: scaleY(0.05); }
    60% { transform: scaleY(1); }
  }
`;

const transitionFade = keyframes`
  0% { opacity: 0; }
  100% { opacity: 1; }
`;

const TransitionOverlay = styled.div<{ $active: boolean }>`
  position: fixed;
  inset: 0;
  z-index: 2000;
  pointer-events: none;
  opacity: ${(p) => (p.$active ? 1 : 0)};
  background: radial-gradient(ellipse 55% 45% at 50% 48%, rgba(255,252,247,0.94) 0%, rgba(253,242,235,0.88) 40%, rgba(240,236,245,0.70) 100%);
  transition: opacity 0.35s ease;
  animation: ${(p) => (p.$active ? transitionFade : "none")} 0.35s ease both;
`;

// ── Scene ──

const Wrapper = styled.div<{ $px: number; $py: number; $strength: number }>`
  width: 100vw; height: 100vh;
  position: relative; overflow: hidden;
  background: url(${bgImage}) center / cover no-repeat;
  background-position: ${(p) => 50 + p.$px * p.$strength * 3}% ${(p) => 50 + p.$py * p.$strength * 2}%;
`;

const FXLayer = styled.div`
  position: absolute; inset: 0; pointer-events: none; z-index: 1;
`;

const VignetteOverlay = styled.div<{ $opacity: number }>`
  position: absolute; inset: 0; pointer-events: none; z-index: 2;
  background: radial-gradient(ellipse at center, transparent 55%, rgba(8, 4, 6, ${(p) => p.$opacity}) 100%);
`;

// ── Card ──

const CardLayer = styled.div`
  position: absolute; inset: 0; z-index: 15;
  display: flex; align-items: center; justify-content: center;
  pointer-events: none;
`;

const Card = styled.div`
  display: flex;
  width: 820px; height: 540px;
  border-radius: 20px;
  overflow: hidden;
  background: oklch(0.94 0.01 20 / 0.75);
  backdrop-filter: blur(24px);
  border: 1px solid oklch(0.9 0.02 20 / 0.3);
  box-shadow: 0 16px 60px rgba(0,0,0,0.15);
  pointer-events: auto;
  animation: ${fadeUp} 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
`;

// ── Character stage ──

const CharStage = styled.div`
  flex: 0 0 400px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, oklch(0.7 0.15 10 / 0.55), oklch(0.68 0.16 15 / 0.65), oklch(0.65 0.15 20 / 0.55));
`;

const StageGrid = styled.div`
  position: absolute; inset: 0; z-index: 1;
  background-image:
    linear-gradient(oklch(1 0 0 / 0.08) 1px, transparent 1px),
    linear-gradient(90deg, oklch(1 0 0 / 0.08) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
`;

const DecorCircle1 = styled.div`
  position: absolute; top: 25%; right: 25%;
  width: 180px; height: 180px; border-radius: 50%;
  background: oklch(1 0 0 / 0.08);
  filter: blur(40px); pointer-events: none;
`;

const DecorCircle2 = styled.div`
  position: absolute; bottom: 25%; left: 25%;
  width: 240px; height: 240px; border-radius: 50%;
  background: oklch(1 0 0 / 0.04);
  filter: blur(48px); pointer-events: none;
`;

const BrandRow = styled.div`
  position: absolute; top: 24px; left: 24px; z-index: 20;
  display: flex; align-items: center; gap: 8px;
  font-size: 14px; font-weight: 500; color: oklch(0.95 0.01 0);
  letter-spacing: 0.04em;
`;

const BrandIcon = styled.div`
  width: 30px; height: 30px; border-radius: 8px;
  background: oklch(0.98 0 0 / 0.12);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
`;

const FooterLinks = styled.div`
  position: absolute; bottom: 20px; left: 24px; z-index: 20;
  display: flex; gap: 20px;
  font-size: 11px; color: oklch(0.92 0.01 0 / 0.5);
  a { color: inherit; text-decoration: none; }
  a:hover { color: oklch(1 0 0); }
`;

const SpiritGuideCard = styled.div<{ $color: string }>`
  position: absolute;
  top: 68px;
  left: 24px;
  right: 24px;
  z-index: 22;
  min-height: 112px;
  padding: 16px 16px 14px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 18px;
  background:
    radial-gradient(circle at 16% 20%, ${(p) => p.$color}44, transparent 34%),
    rgba(255, 255, 255, 0.12);
  color: #fff;
  box-shadow: 0 18px 44px rgba(42, 16, 36, 0.15);
  backdrop-filter: blur(18px) saturate(130%);
  pointer-events: none;

  span {
    display: inline-flex;
    align-items: center;
    min-height: 22px;
    padding: 0 9px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.18);
    color: rgba(255, 255, 255, 0.86);
    font-size: 11px;
    font-family: "Noto Sans SC", "PingFang SC", sans-serif;
  }

  h3 {
    margin: 10px 0 0;
    font-size: 21px;
    letter-spacing: 0.04em;
  }

  p {
    margin: 7px 0 0;
    color: rgba(255, 255, 255, 0.74);
    font-family: "Noto Sans SC", "PingFang SC", sans-serif;
    font-size: 12px;
    line-height: 1.65;
  }
`;

const SpiritStageHint = styled.div`
  position: absolute;
  left: 24px;
  right: 24px;
  top: 190px;
  z-index: 22;
  min-height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.13);
  color: rgba(255, 255, 255, 0.72);
  font-family: "Noto Sans SC", "PingFang SC", sans-serif;
  font-size: 11px;
  letter-spacing: 0.04em;
  pointer-events: none;
`;

// ── Form ──

const FormPanel = styled.div`
  flex: 1;
  display: flex; flex-direction: column; justify-content: center;
  padding: 48px 40px;
  background: transparent;
`;

const Title = styled.h2`
  font-size: 24px; font-weight: 500; margin: 0; letter-spacing: 0.02em;
  color: oklch(0.25 0.02 20);
`;

const Subtitle = styled.p`
  font-size: 13px; color: oklch(0.5 0.02 20); margin: 4px 0 28px;
`;

const Field = styled.div`
  margin-bottom: 14px;
`;

const Label = styled.label`
  display: block;
  font-size: 12px; font-weight: 500; color: oklch(0.4 0.02 20);
  margin-bottom: 5px; letter-spacing: 0.02em;
`;

const Input = styled.input`
  width: 100%; padding: 11px 14px;
  border: 1px solid oklch(0.85 0.02 20 / 0.4); border-radius: 10px;
  background: oklch(0.97 0.005 20 / 0.6);
  color: oklch(0.25 0.01 20); font-size: 13px;
  outline: none; box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s;
  &::placeholder { color: oklch(0.6 0.02 20); }
  &:focus {
    border-color: oklch(0.7 0.13 10 / 0.6);
    box-shadow: 0 0 0 3px oklch(0.7 0.13 10 / 0.1);
  }
`;

const PwdWrap = styled.div` position: relative; `;

const EyeToggle = styled.button`
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  background: none; border: none; padding: 4px;
  color: oklch(0.5 0.02 20); cursor: pointer;
  display: flex; align-items: center;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
  &:hover { color: oklch(0.3 0.03 20); background: oklch(0.85 0.02 20 / 0.3); }
`;

const Row = styled.div`
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 18px; font-size: 12px;
`;

const CheckLabel = styled.label`
  display: flex; align-items: center; gap: 6px;
  color: oklch(0.5 0.02 20); cursor: pointer; font-size: 12px;
`;

const Checkbox = styled.input`
  width: 15px; height: 15px;
  accent-color: oklch(0.7 0.13 10);
  cursor: pointer;
`;

const Link = styled.span`
  color: oklch(0.65 0.12 10); cursor: pointer; font-size: 12px; font-weight: 500;
  &:hover { color: oklch(0.6 0.14 12); }
`;

const CodeBtn = styled.button`
  min-width: 90px; padding: 10px 12px;
  border: 1px solid oklch(0.7 0.13 10 / 0.4); border-radius: 10px;
  background: oklch(0.97 0.005 20 / 0.8);
  color: oklch(0.5 0.05 20); font-size: 12px; font-weight: 600;
  cursor: pointer; white-space: nowrap;
  font-family: "Noto Sans SC","PingFang SC",sans-serif;
  transition: all 0.2s;
  &:hover:not(:disabled) {
    background: oklch(0.7 0.13 10 / 0.1);
    border-color: oklch(0.7 0.13 10 / 0.6);
    color: oklch(0.5 0.12 10);
  }
  &:disabled {
    opacity: 0.5; cursor: not-allowed;
  }
`;

const LoginBtn = styled.button<{ $shaking: boolean; $hovered: boolean }>`
  width: 100%; padding: 11px;
  border: none; border-radius: 10px;
  background: oklch(0.7 0.15 10); color: oklch(0.98 0 0);
  font-size: 14px; font-weight: 500; cursor: pointer; letter-spacing: 0.03em;
  transition: all 0.3s;
  animation: ${(p) => p.$shaking ? shakeAnim : p.$hovered ? popIn : "none"}
    ${(p) => p.$shaking ? "0.5s ease-in-out" : p.$hovered ? "0.35s ease-out both" : ""};
  &:hover { background: oklch(0.67 0.16 12); transform: scale(1.02); }
  &:active { transform: scale(0.97); }
  &:disabled { opacity: 0.6; cursor: not-allowed; }
`;

const ErrorMsg = styled.p`
  color: oklch(0.55 0.18 20); font-size: 12px; text-align: center;
  margin: 10px 0 0; min-height: 18px;
`;

const Divider = styled.div`
  display: flex; align-items: center; gap: 10px;
  margin: 18px 0; font-size: 11px; color: oklch(0.55 0.02 20);
  &::before, &::after { content: ''; flex: 1; height: 1px; background: oklch(0.8 0.02 20 / 0.3); }
`;

const GoogleBtn = styled.button`
  width: 100%; padding: 10px;
  border: 1px solid oklch(0.8 0.02 20 / 0.3); border-radius: 10px;
  background: oklch(0.96 0.005 20 / 0.5); color: oklch(0.35 0.02 20);
  font-size: 13px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
  transition: all 0.3s;
  &:hover { background: oklch(0.92 0.01 20 / 0.6); border-color: oklch(0.7 0.02 20 / 0.4); }
`;

const RegisterHint = styled.p`
  text-align: center; font-size: 12px; color: oklch(0.5 0.02 20); margin-top: 18px;
`;

// ── Helper ──

function cl(v: number, lo: number, hi: number) { return Math.max(lo, Math.min(hi, v)); }

function dist(x1: number, y1: number, x2: number, y2: number) {
  return Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2);
}

/** Breathing animation offset — pure function, defined outside component to avoid recreation */
function idleBreath(phase: number, charIdx: number): number {
  const freq = [0.0018, 0.0021, 0.0015, 0.0024][charIdx];
  return Math.sin(phase * freq + charIdx * 1.2) * 0.02;
}

import SpiritChar from "../components/spirits/SpiritChar";
import { SPIRIT_CONFIGS } from "../components/spirits/spiritConfigs";
import type { SpiritId } from "../components/spirits/types";

// ── Character pose utilities ──

// Proximity stretch config
const STRETCH_RADIUS = 250;
const STRETCH_MAX = 0.28;
interface PointerPoint {
  x: number;
  y: number;
}

interface CharacterPose {
  faceX: number;
  faceY: number;
  bodySkew: number;
  stretch: number;
}

const neutralPose: CharacterPose = { faceX: 0, faceY: 0, bodySkew: 0, stretch: 0 };

// Use SPIRIT_CONFIGS instead of inline definitions
const spiritGuides = {
  purple: SPIRIT_CONFIGS.purple.info,
  black: SPIRIT_CONFIGS.black.info,
  orange: SPIRIT_CONFIGS.orange.info,
  yellow: SPIRIT_CONFIGS.yellow.info,
};

const PURPLE = SPIRIT_CONFIGS.purple.dim;
const BLACK = SPIRIT_CONFIGS.black.dim;
const ORANGE = SPIRIT_CONFIGS.orange.dim;
const YELLOW = SPIRIT_CONFIGS.yellow.dim;

function readCharacterPose(element: HTMLDivElement | null, pointer: PointerPoint, baseH: number): CharacterPose {
  if (!element) return neutralPose;
  const r = element.getBoundingClientRect();
  const cx = r.left + r.width / 2;
  const cy = r.top + r.height / 3;
  const dx = pointer.x - cx;
  const dy = pointer.y - cy;
  const stretchDistance = dist(pointer.x, pointer.y, cx, r.top + r.height * 0.4);
  return {
    faceX: cl(dx / 20, -15, 15),
    faceY: cl(dy / 30, -10, 10),
    bodySkew: cl(-dx / 120, -6, 6),
    stretch: Math.max(0, 1 - stretchDistance / STRETCH_RADIUS) * STRETCH_MAX * baseH,
  };
}

// ── Local error boundary for character stage ──

class StageErrorBoundary extends Component<{ children: React.ReactNode }, { error: Error | null }> {
  state = { error: null as Error | null };
  static getDerivedStateFromError(error: Error) { return { error }; }
  render() {
    if (this.state.error) {
      return (
        <div style={{ flex: "0 0 400px", display: "flex", alignItems: "center", justifyContent: "center", background: "linear-gradient(135deg, oklch(0.7 0.15 10 / 0.55), oklch(0.65 0.15 20 / 0.55))", color: "white", fontFamily: "monospace", fontSize: 11, padding: 20, textAlign: "center" }}>
          <div>
            <p style={{ fontWeight: 600 }}>Character error</p>
            <p style={{ opacity: 0.7, fontSize: 10 }}>{this.state.error.message}</p>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

// ── Main ──

export default function Login() {
  const nav = useNavigate();
  const { s } = useSettings();

  // Scene
  const fxRef = useRef<HTMLDivElement>(null);
  const fxInstanceRef = useRef<ISeasonalFX | null>(null);
  const [mouse, setMouse] = useState({ x: 0, y: 0 });

  // Form
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [codeSending, setCodeSending] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [showPwd, setShowPwd] = useState(false);
  const [registering, setRegistering] = useState(false);
  const [error, setError] = useState("");
  const [shaking, setShaking] = useState(false);
  const [loading, setLoading] = useState(false);
  const [submitHovered, setSubmitHovered] = useState(false);
  const [activeSpirit, setActiveSpirit] = useState<SpiritId>("purple");
  const [resettingPassword, setResettingPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);

  // Character states
  const [isTyping, setIsTyping] = useState(false);
  const [lookingAtEachOther, setLookingAtEachOther] = useState(false);
  const [purplePeeking, setPurplePeeking] = useState(false);
  const [purpleBlinking, setPurpleBlinking] = useState(false);
  const [blackBlinking, setBlackBlinking] = useState(false);
  const [characterPose, setCharacterPose] = useState({
    purple: neutralPose,
    black: neutralPose,
    orange: neutralPose,
    yellow: neutralPose,
  });

  const typingTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Breath
  const [breathT, setBreathT] = useState(0);

  // Refs
  const purpleRef = useRef<HTMLDivElement>(null);
  const blackRef = useRef<HTMLDivElement>(null);
  const orangeRef = useRef<HTMLDivElement>(null);
  const yellowRef = useRef<HTMLDivElement>(null);

  // Mouse tracking — throttled to rAF for smooth 60fps
  const rafId = useRef(0);
  useEffect(() => {
    const onMove = (e: MouseEvent) => {
      if (rafId.current) return;
      rafId.current = requestAnimationFrame(() => {
        rafId.current = 0;
        const mx = (e.clientX / window.innerWidth - 0.5) * 2;
        const my = (e.clientY / window.innerHeight - 0.5) * 2;
        setMouse({ x: mx, y: my });

        const pointer = { x: e.clientX, y: e.clientY };
        setCharacterPose({
          purple: readCharacterPose(purpleRef.current, pointer, PURPLE.h),
          black: readCharacterPose(blackRef.current, pointer, BLACK.h),
          orange: readCharacterPose(orangeRef.current, pointer, ORANGE.h),
          yellow: readCharacterPose(yellowRef.current, pointer, YELLOW.h),
        });
      }) as unknown as number;
    };
    window.addEventListener("mousemove", onMove, { passive: true });
    return () => {
      window.removeEventListener("mousemove", onMove);
      if (rafId.current) cancelAnimationFrame(rafId.current);
    };
  }, []);

  useEffect(() => () => {
    if (typingTimer.current) clearTimeout(typingTimer.current);
  }, []);

  // SeasonalFX
  useEffect(() => {
    if (!fxRef.current) return;
    const fx = createSeasonalFX({
      target: fxRef.current,
      season: "spring",
      seasonConfig: { spring: { variant: "softPetals", intensity: s.petalIntensity } },
      particleCustomization: {
        sizeMultiplier: s.petalSize,
        speedMultiplier: s.petalSpeed,
        customImage: petalImg,
        imageMode: "contain",
      },
      respectReducedMotion: true,
    });
    fx.start();
    fxInstanceRef.current = fx;
    return () => { fx.destroy(); };
  }, [s.petalIntensity, s.petalSize, s.petalSpeed]);

  // Breathing animation (throttled to ~8fps)
  useEffect(() => {
    const id = setInterval(() => {
      setBreathT(performance.now());
    }, 120);
    return () => clearInterval(id);
  }, []);

  // Random blinking
  useEffect(() => {
    const schedule = (setter: (v: boolean) => void) => {
      const blink = () => {
        setter(true);
        setTimeout(() => setter(false), 150);
        timer = setTimeout(blink, Math.random() * 4000 + 3000);
      };
      let timer = setTimeout(blink, Math.random() * 3000 + 2000);
      return () => clearTimeout(timer);
    };
    const c1 = schedule(setPurpleBlinking);
    const c2 = schedule(setBlackBlinking);
    return () => { c1(); c2(); };
  }, []);

  const handleUserFocus = useCallback(() => {
    if (typingTimer.current) clearTimeout(typingTimer.current);
    setIsTyping(true);
    setLookingAtEachOther(true);
    typingTimer.current = setTimeout(() => setLookingAtEachOther(false), 800);
  }, []);

  const handleUserBlur = useCallback(() => {
    if (typingTimer.current) clearTimeout(typingTimer.current);
    typingTimer.current = null;
    setIsTyping(false);
    setLookingAtEachOther(false);
  }, []);

  // Password peeking
  const isPwdVisible = showPwd && password.length > 0;
  useEffect(() => {
    if (!isPwdVisible) return;
    const schedule = () => {
      const peek = () => {
        setPurplePeeking(true);
        setTimeout(() => setPurplePeeking(false), 800);
        timer = setTimeout(peek, Math.random() * 3000 + 2000);
      };
      let timer = setTimeout(peek, Math.random() * 2000 + 1000);
      return () => clearTimeout(timer);
    };
    return schedule();
  }, [isPwdVisible, password, showPwd]);

  const purplePos = characterPose.purple;
  const blackPos = characterPose.black;
  const orangePos = characterPose.orange;
  const yellowPos = characterPose.yellow;
  const activeSpiritInfo = spiritGuides[activeSpirit];

  // Transition — simple fade, no SVG outline
  const [transitioning, setTransitioning] = useState(false);

  function navigateTo(target: string) {
    setTransitioning(true);
    setTimeout(() => nav(target), 420);
  }

  // Countdown timer for resend
  useEffect(() => {
    if (countdown <= 0) return;
    const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    return () => clearTimeout(timer);
  }, [countdown]);

  // Send verification code
  const handleSendCode = async () => {
    const trimmedEmail = email.trim();
    if (!trimmedEmail || !trimmedEmail.includes("@")) {
      setError("请先输入有效的邮箱地址");
      return;
    }
    setError("");
    setCodeSending(true);
    try {
      await api.post("/auth/send-code", { email: trimmedEmail });
      setCountdown(60);
      setError("验证码已发送，开发环境请查看后端控制台日志获取验证码");
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || "发送失败";
      setError(msg);
    } finally {
      setCodeSending(false);
    }
  };

  // Submit — real API login/register
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setError(registering ? "请填写用户名和密码完成注册" : "请输入用户名和密码");
      setShaking(true);
      setTimeout(() => setShaking(false), 500);
      return;
    }
    setError("");
    setLoading(true);
    try {
      const endpoint = registering ? "/auth/register" : "/auth/login";
      const body: any = { username, password };
      if (registering && email.trim()) {
        body.email = email.trim();
        body.code = code;
      }
      const { data } = await api.post(endpoint, body);
      // Store token
      if (data.token) {
        localStorage.setItem("token", data.token);
        if (data.refreshToken) localStorage.setItem("refreshToken", data.refreshToken);
        if (data.username) localStorage.setItem("username", data.username);
      }
      navigateTo("/me");
    } catch (err: any) {
      // 优先取后端返回的错误消息，其次 axios error message
      const msg = err?.response?.data?.message || err?.message || "操作失败，请重试";
      setError(msg);
      setShaking(true);
      setTimeout(() => setShaking(false), 500);
    } finally {
      setLoading(false);
    }
  };

  // Reset password via email + code
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !email.includes("@")) {
      setError("请先输入有效的邮箱地址");
      return;
    }
    if (!code || !password) {
      setError("请输入验证码和新密码");
      return;
    }
    setError("");
    setLoading(true);
    try {
      await api.post("/auth/reset-password", { email: email.trim(), code, newPassword: password });
      setError("密码重置成功，请登录");
      setResettingPassword(false);
      setPassword("");
      setCode("");
      setCountdown(0);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || "重置失败";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleSpiritClick = useCallback((spirit: SpiritId, event?: React.MouseEvent<HTMLDivElement>) => {
    event?.stopPropagation();
    setActiveSpirit(spirit);
  }, []);

  const handleSpiritKey = useCallback((spirit: SpiritId, event: React.KeyboardEvent<HTMLDivElement>) => {
    if (event.key !== "Enter" && event.key !== " ") return;
    event.preventDefault();
    setActiveSpirit(spirit);
  }, []);

  return (
    <Wrapper $px={mouse.x} $py={mouse.y} $strength={s.parallaxStrength}>
      <FXLayer ref={fxRef} />
      <Bokeh />
      <Komorebi />
      <GroundPetals />
      <VignetteOverlay $opacity={s.vignetteOpacity} />
      <DeskPet />
      <InlineKeyframes />

      <CardLayer>
        <Card>
          {/* ── Left: Character Stage ── */}
          <CharStage>
            <StageErrorBoundary>
            <StageGrid />
            <DecorCircle1 />
            <DecorCircle2 />

            <BrandRow>
              <BrandIcon>◎</BrandIcon>
              江苏高校生活指北
            </BrandRow>

            <SpiritGuideCard $color={activeSpiritInfo.color}>
              <span>{activeSpiritInfo.name}</span>
              <h3>{activeSpiritInfo.title}</h3>
              <p>{activeSpiritInfo.description}</p>
            </SpiritGuideCard>

            <SpiritStageHint>点击四个小精灵，查看它们负责的网站能力</SpiritStageHint>

            <FooterLinks>
              <a href="#" onClick={(e) => { e.preventDefault(); setError("隐私政策页面建设中"); }}>隐私政策</a>
              <a href="#" onClick={(e) => { e.preventDefault(); setError("服务条款页面建设中"); }}>服务条款</a>
              <a href="#" onClick={(e) => { e.preventDefault(); setError("联系方式: admin@jiangsu.guide"); }}>联系</a>
            </FooterLinks>

            <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "flex-end", justifyContent: "center" }}>
              <div style={{ position: "relative", width: 400, height: 310 }}>

                {/* Char 0 — Purple */}
                <SpiritChar
                  ref={purpleRef}
                  config={SPIRIT_CONFIGS.purple}
                  isActive={activeSpirit === "purple"}
                  transform={((): string => {
                    const lean = isPwdVisible ? 0
                      : (isTyping || (password.length > 0 && !showPwd)) ? (purplePos.bodySkew || 0) * 0.8 - 8
                      : submitHovered ? (purplePos.bodySkew || 0) * 0.8 - 3
                      : (purplePos.bodySkew || 0) * 0.8;
                    const tx = (isTyping || (password.length > 0 && !showPwd)) ? 30 : 0;
                    const breath = 1 + idleBreath(breathT, 0);
                    const stretch = 1 + purplePos.stretch / PURPLE.h;
                    return "skewX(" + lean.toFixed(1) + "deg) translateX(" + tx + "px) scaleY(" + (breath * stretch).toFixed(3) + ")";
                  })()}
                  eyeTransform={((): string => {
                    const ex = isPwdVisible ? (purplePeeking ? 4 : -4) : (lookingAtEachOther ? 3 : purplePos.faceX * 0.3);
                    const ey = isPwdVisible ? (purplePeeking ? 5 : -4) : (lookingAtEachOther ? 4 : purplePos.faceY * 0.3);
                    return "translateX(-50%) translate(" + ex.toFixed(1) + "px," + ey.toFixed(1) + "px)";
                  })()}
                  lookX={isPwdVisible ? (purplePeeking ? 4 : -4) : lookingAtEachOther ? 3 : undefined}
                  lookY={isPwdVisible ? (purplePeeking ? 5 : -4) : lookingAtEachOther ? 4 : undefined}
                  isBlinking={purpleBlinking}
                  heightOverride={isTyping ? PURPLE.hTall : undefined}
                  onActivate={(event) => handleSpiritClick("purple", event)}
                  onKeyActivate={(event) => handleSpiritKey("purple", event)}
                />

                {/* Char 1 — Black */}
                <SpiritChar
                  ref={blackRef}
                  config={SPIRIT_CONFIGS.black}
                  isActive={activeSpirit === "black"}
                  haloColor="rgba(255,255,255,0.78)"
                  transform={((): string => {
                    const lean = isPwdVisible ? 0
                      : lookingAtEachOther ? (blackPos.bodySkew || 0) * 0.5 + 6
                      : (isTyping || (password.length > 0 && !showPwd)) ? (blackPos.bodySkew || 0) * 0.7
                      : submitHovered ? (blackPos.bodySkew || 0) * 0.7 - 3
                      : (blackPos.bodySkew || 0) * 0.7;
                    const tx = lookingAtEachOther ? 15 : 0;
                    const breath = 1 + idleBreath(breathT, 1);
                    const stretch = 1 + blackPos.stretch / BLACK.h;
                    return "skewX(" + lean.toFixed(1) + "deg) translateX(" + tx + "px) scaleY(" + (breath * stretch).toFixed(3) + ")";
                  })()}
                  eyeTransform={((): string => {
                    const ex = isPwdVisible ? -4 : (lookingAtEachOther ? 0 : blackPos.faceX * 0.3);
                    const ey = isPwdVisible ? -4 : (lookingAtEachOther ? -4 : blackPos.faceY * 0.3);
                    return "translateX(-50%) translate(" + ex.toFixed(1) + "px," + ey.toFixed(1) + "px)";
                  })()}
                  lookX={isPwdVisible ? -4 : lookingAtEachOther ? 0 : undefined}
                  lookY={isPwdVisible ? -4 : lookingAtEachOther ? -4 : undefined}
                  isBlinking={blackBlinking}
                  onActivate={(event) => handleSpiritClick("black", event)}
                  onKeyActivate={(event) => handleSpiritKey("black", event)}
                />

                {/* Char 2 — Orange */}
                <SpiritChar
                  ref={orangeRef}
                  config={SPIRIT_CONFIGS.orange}
                  isActive={activeSpirit === "orange"}
                  transform={((): string => {
                    const lean = isPwdVisible ? 0 : submitHovered ? (orangePos.bodySkew || 0) * 0.6 - 2 : (orangePos.bodySkew || 0) * 0.6;
                    const breath = 1 + idleBreath(breathT, 2);
                    const stretch = 1 + orangePos.stretch / ORANGE.h;
                    return "skewX(" + lean.toFixed(1) + "deg) scaleY(" + (breath * stretch).toFixed(3) + ")";
                  })()}
                  eyeTransform={((): string => {
                    const ex = isPwdVisible ? -4 : orangePos.faceX * 0.3;
                    const ey = isPwdVisible ? -3 : orangePos.faceY * 0.3;
                    return "translateX(-50%) translate(" + ex.toFixed(1) + "px," + ey.toFixed(1) + "px)";
                  })()}
                  lookX={isPwdVisible ? -5 : undefined}
                  lookY={isPwdVisible ? -4 : undefined}
                  onActivate={(event) => handleSpiritClick("orange", event)}
                  onKeyActivate={(event) => handleSpiritKey("orange", event)}
                />

                {/* Char 3 — Yellow */}
                <SpiritChar
                  ref={yellowRef}
                  config={SPIRIT_CONFIGS.yellow}
                  isActive={activeSpirit === "yellow"}
                  transform={((): string => {
                    const lean = isPwdVisible ? 0 : submitHovered ? (yellowPos.bodySkew || 0) * 0.6 - 2 : (yellowPos.bodySkew || 0) * 0.6;
                    const breath = 1 + idleBreath(breathT, 3);
                    const stretch = 1 + yellowPos.stretch / YELLOW.h;
                    return "skewX(" + lean.toFixed(1) + "deg) scaleY(" + (breath * stretch).toFixed(3) + ")";
                  })()}
                  eyeTransform={((): string => {
                    const ex = isPwdVisible ? -4 : yellowPos.faceX * 0.3;
                    const ey = isPwdVisible ? -3 : yellowPos.faceY * 0.3;
                    return "translateX(-50%) translate(" + ex.toFixed(1) + "px," + ey.toFixed(1) + "px)";
                  })()}
                  lookX={isPwdVisible ? -5 : undefined}
                  lookY={isPwdVisible ? -4 : undefined}
                  onActivate={(event) => handleSpiritClick("yellow", event)}
                  onKeyActivate={(event) => handleSpiritKey("yellow", event)}
                />

              </div>
            </div>
            </StageErrorBoundary>
          </CharStage>

          {/* ── Right: Form ── */}
          <FormPanel>
            {resettingPassword ? (
              <>
                <Title>重置密码</Title>
                <Subtitle>输入注册邮箱，获取验证码后设置新密码</Subtitle>
                <form onSubmit={handleResetPassword}>
                  <Field>
                    <Label>注册邮箱</Label>
                    <div style={{ display: "flex", gap: 8 }}>
                      <Input
                        type="email" placeholder="输入注册时使用的邮箱"
                        value={email}
                        onChange={(e) => { setEmail(e.target.value); setError(""); }}
                        style={{ flex: 1 }}
                        autoComplete="email"
                      />
                      <CodeBtn
                        type="button"
                        onClick={handleSendCode}
                        disabled={codeSending || countdown > 0 || !email.trim()}
                      >
                        {codeSending ? "发送中..." : countdown > 0 ? `${countdown}s` : "发送验证码"}
                      </CodeBtn>
                    </div>
                  </Field>
                  <Field>
                    <Label>验证码</Label>
                    <Input
                      type="text" placeholder="输入6位验证码"
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      maxLength={6}
                      autoComplete="one-time-code"
                    />
                  </Field>
                  <Field>
                    <Label>新密码</Label>
                    <PwdWrap>
                      <Input
                        type={showPwd ? "text" : "password"}
                        placeholder="输入新密码（至少6位）"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                      />
                      <EyeToggle type="button" onClick={() => setShowPwd(!showPwd)}>
                        {showPwd ? <EyeOff size={18} /> : <Eye size={18} />}
                      </EyeToggle>
                    </PwdWrap>
                  </Field>
                  <LoginBtn
                    type="submit"
                    $shaking={shaking}
                    $hovered={submitHovered}
                    disabled={loading}
                    onMouseEnter={() => setSubmitHovered(true)}
                    onMouseLeave={() => setSubmitHovered(false)}
                  >
                    {loading ? "正在重置..." : "重置密码"}
                  </LoginBtn>
                  <ErrorMsg>{error}</ErrorMsg>
                  <RegisterHint>
                    <Link onClick={() => { setResettingPassword(false); setError(""); setEmail(""); setCode(""); setPassword(""); setCountdown(0); }}>← 返回登录</Link>
                  </RegisterHint>
                </form>
              </>
            ) : (
              <>
            <Title>{registering ? "注册账号" : "欢迎回来"}</Title>
            <Subtitle>{registering ? "创建新账号，开始探索江苏高校" : "登录后进入江苏高校生活指北，继续探索你的校园路线"}</Subtitle>
            <form onSubmit={handleSubmit}>
              <Field>
                <Label>{registering ? "用户名" : "用户名 / 邮箱"}</Label>
                <Input
                  type="text" placeholder={registering ? "输入用户名" : "输入用户名或邮箱"}
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  onFocus={handleUserFocus}
                  onBlur={handleUserBlur}
                  autoComplete="username"
                />
              </Field>
              {registering && (
                <>
                  <Field>
                    <Label>邮箱（选填，绑定后可邮箱登录）</Label>
                    <div style={{ display: "flex", gap: 8 }}>
                      <Input
                        type="email" placeholder="输入邮箱地址"
                        value={email}
                        onChange={(e) => { setEmail(e.target.value); setError(""); }}
                        style={{ flex: 1 }}
                        autoComplete="email"
                      />
                      <CodeBtn
                        type="button"
                        onClick={handleSendCode}
                        disabled={codeSending || countdown > 0 || !email.trim()}
                      >
                        {codeSending ? "发送中..." : countdown > 0 ? `${countdown}s` : "发送验证码"}
                      </CodeBtn>
                    </div>
                  </Field>
                  <Field>
                    <Label>验证码</Label>
                    <Input
                      type="text" placeholder="输入6位验证码"
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      maxLength={6}
                      autoComplete="one-time-code"
                    />
                  </Field>
                </>
              )}
              <Field>
                <Label>密码</Label>
                <PwdWrap>
                  <Input
                    type={showPwd ? "text" : "password"}
                    placeholder="输入密码"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <EyeToggle type="button" onClick={() => setShowPwd(!showPwd)}>
                    {showPwd ? <EyeOff size={18} /> : <Eye size={18} />}
                  </EyeToggle>
                </PwdWrap>
              </Field>
              <Row>
                <CheckLabel>
                  <Checkbox type="checkbox" checked={rememberMe} onChange={(e) => setRememberMe(e.target.checked)} />
                  记住我
                </CheckLabel>
                <Link onClick={() => { setResettingPassword(true); setError(""); setRegistering(false); }}>忘记密码？</Link>
              </Row>
              <LoginBtn
                type="submit"
                $shaking={shaking}
                $hovered={submitHovered}
                disabled={loading}
                onMouseEnter={() => setSubmitHovered(true)}
                onMouseLeave={() => setSubmitHovered(false)}
              >
                {loading ? "正在处理..." : registering ? "注册并进入" : "进入校园入口"}
              </LoginBtn>
              <ErrorMsg>{error}</ErrorMsg>
              <Divider>或者</Divider>
              <GoogleBtn type="button" onClick={() => navigateTo("/me")}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                游客体验（无需登录）
              </GoogleBtn>
              <RegisterHint>
                {registering ? "已有账号？" : "还没有账号？"} <Link onClick={() => { setRegistering(!registering); setError(""); setEmail(""); setCode(""); setCountdown(0); }}>{registering ? "返回登录" : "注册新账号"}</Link>
              </RegisterHint>
            </form>
              </>
            )}
          </FormPanel>
        </Card>
      </CardLayer>
      {transitioning && <TransitionOverlay $active={transitioning} />}
    </Wrapper>
  );
}
