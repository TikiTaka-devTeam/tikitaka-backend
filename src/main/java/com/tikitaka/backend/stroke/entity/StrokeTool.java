package com.tikitaka.backend.stroke.entity;

public enum StrokeTool {
    PEN,         // private, shared
    HIGHLIGHTER, // private, shared
    ERASER,      // private, shared
    Q_POINT,     // private only (학생 전용, 교수 shared 사용 불가)
    Q_LIST,      // private, shared
    KEYBOARD,    // private, shared
    FIXER,       // private only
    ETC          // private, shared
}
