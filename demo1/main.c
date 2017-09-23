#ifdef __WIN32__
#include <mman.h>
#else
#include <sys/mman.h>
#endif
typedef unsigned (*asmFunc)(void);
int main () {
	unsigned char* data = mmap(0, 8*1024, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_ANON | MAP_PRIVATE, 0, 0);
	unsigned char* bytes = data;
	*bytes++ = 0xb0;
	*bytes++ = 0x1;
	*bytes++ = 0xb1;
	*bytes++ = 0x2;
	*bytes++ = 0x0;
	*bytes++ = 0xc8;
	*bytes++ = 0xc3;
	asmFunc func = (asmFunc) data;
	return func();
}