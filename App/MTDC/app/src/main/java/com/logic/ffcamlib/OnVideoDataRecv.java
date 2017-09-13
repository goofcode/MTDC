package com.logic.ffcamlib;

public interface OnVideoDataRecv {
    void audioData(byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3);
    void dataRecv(int paramInt1, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4);
}
