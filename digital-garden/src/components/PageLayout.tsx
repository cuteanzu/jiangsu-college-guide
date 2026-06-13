import styled from "styled-components";

const PageLayout = styled.div`
  height: 100%;
  padding: 28px 32px 48px;
  background: linear-gradient(170deg, #fdf7f2 0%, #f7efe4 40%, #f0e8db 100%);
  color: #3a2f28;
  font-family: "Noto Serif SC", "Songti SC", "STSong", "KaiTi", serif;
  overflow-y: auto;
  box-sizing: border-box;

  @media (max-width: 640px) {
    padding: 16px 16px 24px;
  }
`;

export default PageLayout;
