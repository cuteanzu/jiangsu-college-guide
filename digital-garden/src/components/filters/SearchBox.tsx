import styled from "styled-components";
import { Search as SearchIcon } from "lucide-react";
import type { ChangeEvent } from "react";

const Wrapper = styled.div`
  position: relative;
  max-width: 360px;
  margin-bottom: 20px;

  svg {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #b8a090;
    width: 16px;
    height: 16px;
  }

  input {
    width: 100%;
    padding: 10px 14px 10px 36px;
    border: 1px solid rgba(180, 150, 130, 0.3);
    border-radius: 12px;
    background: rgba(255, 252, 247, 0.7);
    font-size: 14px;
    font-family: inherit;
    color: #3a2f28;
    outline: none;
    box-sizing: border-box;
    transition: border 0.2s;

    &:focus {
      border-color: #c76b5e;
    }

    &::placeholder {
      color: #b8a090;
    }
  }
`;

interface SearchBoxProps {
  placeholder: string;
  value: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
}

export default function SearchBox({ placeholder, value, onChange }: SearchBoxProps) {
  return (
    <Wrapper>
      <SearchIcon />
      <input
        placeholder={placeholder}
        value={value}
        onChange={onChange}
      />
    </Wrapper>
  );
}
