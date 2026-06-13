import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import styled from "styled-components";
import {
  User,
  Bookmark,
  Edit3,
  Heart,
  LogOut,
  MapPin,
  Settings,
  ExternalLink,
  Loader2,
} from "lucide-react";
import { useCurrentUser } from "../services/hooks";
import api from "../services/api";

import { fadeUp } from "../components/animations";
import PageLayout from "../components/PageLayout";

const Shell = styled.div`
  max-width: 860px;
  margin: 0 auto;
  padding: 40px 32px 80px;

  @media (max-width: 640px) {
    padding: 24px 16px 48px;
  }
`;

// ── Profile header ──

const ProfileCard = styled.div`
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 40px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(180, 150, 120, 0.15);
  backdrop-filter: blur(12px);
  animation: ${fadeUp} 0.5s ease-out;

  @media (max-width: 560px) {
    flex-direction: column;
    text-align: center;
    padding: 28px 20px;
  }
`;

const Avatar = styled.div`
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8d5c4, #d4b896);
  border: 3px solid rgba(180, 140, 100, 0.25);
  display: grid;
  place-items: center;
  flex-shrink: 0;
  color: #8b6f5a;
`;

const ProfileInfo = styled.div`
  flex: 1;
  min-width: 0;
`;

const Username = styled.h1`
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 900;
  letter-spacing: 0.5px;
`;

const Bio = styled.p`
  margin: 0 0 16px;
  font-size: 15px;
  color: #6b5d4f;
  line-height: 1.7;
`;

const MetaRow = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #8b7b6a;
`;

const MetaItem = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 5px;
`;

const EditButton = styled.button`
  margin-left: auto;
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid rgba(180, 140, 100, 0.25);
  background: rgba(255, 255, 255, 0.6);
  color: #6b5d4f;
  font-family: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(180, 140, 100, 0.4);
    color: #3a2f28;
  }

  @media (max-width: 560px) {
    margin-left: 0;
    margin-top: 12px;
  }
`;

const LogoutButton = styled.button`
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid rgba(180, 140, 100, 0.12);
  background: transparent;
  color: #b5a08a;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  margin-left: 8px;

  &:hover {
    color: #c76b5e;
    border-color: rgba(199, 107, 94, 0.2);
    background: rgba(199, 107, 94, 0.04);
  }

  @media (max-width: 560px) {
    margin-left: 0;
    margin-top: 8px;
  }
`;

// ── Stats grid ──

const StatsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 24px;
  animation: ${fadeUp} 0.5s ease-out 0.1s both;

  @media (max-width: 560px) {
    grid-template-columns: repeat(2, 1fr);
  }
`;

const StatCard = styled.div`
  padding: 24px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.45);
  border: 1px solid rgba(180, 150, 120, 0.12);
  text-align: center;
`;

const StatNumber = styled.div`
  font-size: 32px;
  font-weight: 900;
  color: #c76b5e;
  line-height: 1;
`;

const StatLabel = styled.div`
  margin-top: 8px;
  font-size: 13px;
  color: #8b7b6a;
`;

// ── Tab bar ──

const Tabs = styled.div`
  display: flex;
  gap: 0;
  margin-top: 36px;
  border-bottom: 1px solid rgba(180, 150, 120, 0.15);
  animation: ${fadeUp} 0.5s ease-out 0.2s both;
`;

const Tab = styled.button<{ $active: boolean }>`
  padding: 12px 28px;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 15px;
  font-weight: ${(p) => (p.$active ? 800 : 500)};
  color: ${(p) => (p.$active ? "#3a2f28" : "#8b7b6a")};
  cursor: pointer;
  position: relative;
  transition: color 0.2s;

  &::after {
    content: "";
    position: absolute;
    bottom: -1px;
    left: 0;
    right: 0;
    height: 2px;
    background: #c76b5e;
    transform: scaleX(${(p) => (p.$active ? 1 : 0)});
    transition: transform 0.25s ease;
  }

  &:hover {
    color: #3a2f28;
  }

  @media (max-width: 560px) {
    padding: 10px 16px;
    font-size: 14px;
  }
`;

// ── Content area ──

const ContentArea = styled.div`
  margin-top: 28px;
  animation: ${fadeUp} 0.5s ease-out 0.25s both;
`;

const EmptyState = styled.div`
  text-align: center;
  padding: 64px 20px;
  color: #8b7b6a;
`;

const EmptyIcon = styled.div`
  margin-bottom: 16px;
  opacity: 0.4;
`;

const EmptyText = styled.p`
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
`;

const EmptyLink = styled(Link)`
  display: inline-block;
  margin-top: 16px;
  padding: 10px 24px;
  border-radius: 8px;
  border: 1px solid rgba(199, 107, 94, 0.25);
  background: rgba(199, 107, 94, 0.06);
  color: #c76b5e;
  font-family: inherit;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.2s;

  &:hover {
    background: rgba(199, 107, 94, 0.12);
  }
`;

// ── Footer ──

const PageFooter = styled.footer`
  margin-top: 60px;
  padding-top: 24px;
  border-top: 1px solid rgba(180, 150, 120, 0.12);
  text-align: center;
  font-size: 12px;
  color: #b5a592;
`;

const TAB_LABELS = ["收藏的高校", "我的经验", "我的问答", "浏览记录"] as const;

export default function Me() {
  const [tab, setTab] = useState(0);
  const [editing, setEditing] = useState(false);
  const [editNickname, setEditNickname] = useState("");
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();
  const { data: user, loading, error, refetch } = useCurrentUser();

  // Redirect to login if not authenticated
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token || error) {
      navigate("/login", { replace: true });
    }
  }, [loading, error, navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("username");
    navigate("/login");
  };

  const handleEditClick = () => {
    setEditNickname(user?.nickname || "");
    setEditing(true);
  };

  const handleSaveProfile = async () => {
    if (!editNickname.trim()) return;
    setSaving(true);
    try {
      await api.put("/auth/profile", { nickname: editNickname.trim() });
      refetch();
      setEditing(false);
    } catch (err: any) {
      const msg = err?.response?.data?.message || "保存失败";
      alert(msg);
    } finally {
      setSaving(false);
    }
  };

  const displayName = user?.nickname || user?.username || "访客";
  const joinDate = user?.userId ? "2026" : "—";

  if (loading) {
    return (
      <PageLayout>
        <Shell>
          <div style={{ textAlign: "center", padding: "120px 20px", color: "#8b7b6a" }}>
            <Loader2 size={32} style={{ animation: "spin 1s linear infinite", marginBottom: 16 }} />
            <p>加载中...</p>
          </div>
        </Shell>
      </PageLayout>
    );
  }

  if (error || !user) {
    return (
      <PageLayout>
        <Shell>
          <div style={{ textAlign: "center", padding: "120px 20px", color: "#8b7b6a" }}>
            <p>无法加载用户信息，请重新登录</p>
            <EmptyLink to="/login">去登录</EmptyLink>
          </div>
        </Shell>
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <Shell>
        <ProfileCard>
          <Avatar>
            <User size={44} />
          </Avatar>
          <ProfileInfo>
            {editing ? (
              <div style={{ display: "flex", gap: 8, alignItems: "center", flexWrap: "wrap" }}>
                <input
                  value={editNickname}
                  onChange={(e) => setEditNickname(e.target.value)}
                  style={{
                    padding: "8px 12px", borderRadius: 8,
                    border: "1px solid rgba(180,140,100,0.3)",
                    fontSize: 20, fontWeight: 900, fontFamily: "inherit",
                    color: "#3a2f28", background: "rgba(255,255,255,0.7)",
                    width: 200,
                  }}
                  autoFocus
                  onKeyDown={(e) => { if (e.key === "Enter") handleSaveProfile(); if (e.key === "Escape") setEditing(false); }}
                />
                <button
                  onClick={handleSaveProfile}
                  disabled={saving}
                  style={{
                    padding: "6px 16px", borderRadius: 8, border: "none",
                    background: "#c76b5e", color: "#fff", fontFamily: "inherit",
                    fontSize: 13, fontWeight: 700, cursor: "pointer",
                  }}
                >
                  {saving ? "保存中..." : "保存"}
                </button>
                <button
                  onClick={() => setEditing(false)}
                  style={{
                    padding: "6px 12px", borderRadius: 8,
                    border: "1px solid rgba(180,140,100,0.2)",
                    background: "transparent", color: "#8b7b6a",
                    fontFamily: "inherit", fontSize: 13, cursor: "pointer",
                  }}
                >
                  取消
                </button>
              </div>
            ) : (
              <Username>{displayName}</Username>
            )}
            <Bio>探索江苏高校，记录择校之旅。</Bio>
            <MetaRow>
              <MetaItem>
                <MapPin size={13} />
                江苏
              </MetaItem>
              <MetaItem>
                <ExternalLink size={13} />
                加入于 {joinDate}
              </MetaItem>
            </MetaRow>
          </ProfileInfo>
          <EditButton onClick={handleEditClick}>
            <Settings size={14} />
            编辑资料
          </EditButton>
          <LogoutButton onClick={handleLogout}>
            <LogOut size={14} />
            退出
          </LogoutButton>
        </ProfileCard>

        <StatsGrid>
          <StatCard>
            <StatNumber>0</StatNumber>
            <StatLabel>收藏高校</StatLabel>
          </StatCard>
          <StatCard>
            <StatNumber>0</StatNumber>
            <StatLabel>经验分享</StatLabel>
          </StatCard>
          <StatCard>
            <StatNumber>0</StatNumber>
            <StatLabel>问答参与</StatLabel>
          </StatCard>
          <StatCard>
            <StatNumber>0</StatNumber>
            <StatLabel>浏览记录</StatLabel>
          </StatCard>
        </StatsGrid>

        <Tabs>
          {TAB_LABELS.map((label, i) => (
            <Tab key={i} $active={tab === i} onClick={() => setTab(i)}>
              {label}
            </Tab>
          ))}
        </Tabs>

        <ContentArea>
          {tab === 0 && (
            <EmptyState>
              <EmptyIcon>
                <Bookmark size={48} />
              </EmptyIcon>
              <EmptyText>还没有收藏任何高校</EmptyText>
              <EmptyLink to="/jiangsu">去地图看看</EmptyLink>
            </EmptyState>
          )}
          {tab === 1 && (
            <EmptyState>
              <EmptyIcon>
                <Edit3 size={48} />
              </EmptyIcon>
              <EmptyText>还没有分享校园经验</EmptyText>
              <EmptyLink to="/experiences">浏览经验</EmptyLink>
            </EmptyState>
          )}
          {tab === 2 && (
            <EmptyState>
              <EmptyIcon>
                <Heart size={48} />
              </EmptyIcon>
              <EmptyText>还没有参与问答</EmptyText>
              <EmptyLink to="/qa">去问答看看</EmptyLink>
            </EmptyState>
          )}
          {tab === 3 && (
            <EmptyState>
              <EmptyIcon>
                <MapPin size={48} />
              </EmptyIcon>
              <EmptyText>暂无浏览记录</EmptyText>
              <EmptyLink to="/jiangsu">开始探索</EmptyLink>
            </EmptyState>
          )}
        </ContentArea>

        <PageFooter>江苏高校地图 · 开源项目</PageFooter>
      </Shell>
    </PageLayout>
  );
}
