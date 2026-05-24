bits 64
default rel

section .data
    msg: db "Hello, World!", 10

section .text
    global main
	    
    extern WriteConsoleA
    extern GetStdHandle
    extern ExitProcess
    
main:
    push rbp                    ; Prologue
    mov rbp, rsp

    mov rcx, -11                ; STD_OUTPUT_HANDLE
    call GetStdHandle

    push 0                      ; lpReserved
    mov r9, 0                   ; charsWritten
    mov r8, 14                  ; toWrite
    mov rdx, msg                ; message
    mov rcx, rax                ; handle
    call WriteConsoleA

    xor rax, rax                ; Epilogue
    mov rsp, rbp                
    pop rbp
    
    mov rcx, 0
    call ExitProcess    
    ret