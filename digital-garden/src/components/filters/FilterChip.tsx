import styled from "styled-components";

const FilterChip = styled.button<{ $active: boolean; $color: string }>`
  background: ${(p) => (p.$active ? p.$color : "rgba(255,252,247,0.7)")};
  color: ${(p) => (p.$active ? "#fff" : "#6b5d53")};
  border: 1px solid ${(p) => (p.$active ? p.$color : "rgba(180,150,130,0.3)")};
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;

  &:hover {
    border-color: ${(p) => p.$color};
    color: ${(p) => (p.$active ? "#fff" : p.$color)};
  }
`;

export default FilterChip;
