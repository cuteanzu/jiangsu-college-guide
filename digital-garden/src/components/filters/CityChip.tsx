import styled from "styled-components";

const CityChip = styled.button<{ $active: boolean }>`
  background: ${(p) => (p.$active ? "rgba(90,158,124,0.14)" : "rgba(255,252,247,0.5)")};
  color: ${(p) => (p.$active ? "#5a9e7c" : "#8b7d73")};
  border: 1px solid ${(p) => (p.$active ? "rgba(90,158,124,0.25)" : "rgba(180,150,130,0.15)")};
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.18s ease;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 3px;

  &:hover {
    border-color: rgba(90,158,124,0.3);
    color: #5a9e7c;
  }
`;

export default CityChip;
