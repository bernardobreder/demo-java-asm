start:

 mov ax, 4F02h		;set VESA mode
 mov bx, 100h		;640 x 400 x 8bit
 int 10h

 mov ax, 4F05h		;switch to bank 0 to start
 mov bx, 0
 mov dx, 0
 int 10h

 mov ax, 0a000h		;point to video mem
 mov ds, ax

			;----------------------------
			;code to plot pixel. requires
			;finding bank
			;----------------------------
 mov ax, y              
 mov bx, Xres		
 mul bx			;(y*Xres)

 shl edx, 16
 add edx, eax
 mov offsett, edx       ;result of (y*Xres) into offsett
 
 mov ebx, 0
 mov bx, x
 add offsett, ebx	;finally, (y*Xres)+x  is in offsett

 mov [offsett], 2
 
 mov ax, 0100h		;wait for key
 int 21h
 mov ax, 4c00h		;exit
 int 21h

end start
