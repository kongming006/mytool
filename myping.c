#include <stdio.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/ip_icmp.h>
#include <netdb.h>
#include <errno.h>

#define PACKET_SIZE	4096
#define ICMP_DATA_LEN 56

pid_t pid;

/*两个timeval结构相减*/
void tv_sub(struct timeval *out, struct timeval *in )
{
	if ( (out->tv_usec -= in->tv_usec) < 0 )
	{
		--out->tv_sec;
		out->tv_usec += 1000000;
	}
	out->tv_sec -= in->tv_sec;
}

/*校验和算法*/
unsigned short cal_chksum( unsigned short *addr, int len )
{
	int nleft = len;
	int	sum	= 0;
	unsigned short *w = addr;
	unsigned short answer = 0;

    /*把ICMP报头二进制数据以2字节为单位累加起来*/
	while ( nleft > 1 )
	{
		sum	+= *w++;
		nleft	-= 2;
	}
	/* 若ICMP报头为奇数个字节，会剩下最后一字节。
     * 把最后一个字节视为一个2字节数据的高字节，
     * 这个2字节数据的低字节为0，继续累加
     */
	if ( nleft == 1 )
	{
		*(unsigned char *) (&answer) = *(unsigned char *) w;
		sum += answer;
	}
	sum	= (sum >> 16) + (sum & 0xffff);
	sum	+= (sum >> 16);
	answer = ~sum;
	return (answer);
}


/*设置ICMP报头*/
int pack(int pack_no, char *packet)
{
	int		i, packsize;
	struct icmp	*icmp;
	struct timeval	*tval;
	
    icmp = (struct icmp *)packet;
	icmp->icmp_type	= ICMP_ECHO;
	icmp->icmp_code	= 0;
	icmp->icmp_cksum = 0;
	icmp->icmp_seq = pack_no;
	icmp->icmp_id = pid;
	packsize = 8 + ICMP_DATA_LEN;
	tval = (struct timeval *)icmp->icmp_data;
    
    /*记录发送时间*/
	gettimeofday(tval, NULL);
	
    /*校验算法*/
    icmp->icmp_cksum = cal_chksum( (unsigned short *) icmp, packsize );
	
    return (packsize);
}

/*剥去ICMP报头*/
int unpack(struct sockaddr_in *from, char *buf, int len, 
        struct timeval *tvrecv)
{
	int	i, iphdrlen;
	struct ip	*ip;
	struct icmp	*icmp;
	struct timeval	*tvsend;
	double rtt;
	ip = (struct ip *) buf;
	iphdrlen = ip->ip_hl << 2;                       /*求ip报头长度,即ip报头的长度标志乘4*/
	icmp = (struct icmp *)(buf + iphdrlen);  /*越过ip报头,指向ICMP报头*/
	len	-= iphdrlen;                            /*ICMP报头及ICMP数据报的总长度*/
	if ( len < 8 ) {
        /*小于ICMP报头长度则不合理*/
		printf( "ICMP packets\'s length is less than 8\n" );
		return(-1);
	}
	/*确保所接收的是我所发的的ICMP的回应*/
	if ((icmp->icmp_type == ICMP_ECHOREPLY) && (icmp->icmp_id == pid)) {
		tvsend = (struct timeval *) icmp->icmp_data;
		tv_sub( tvrecv, tvsend );                              /*接收和发送的时间差*/
		rtt = tvrecv->tv_sec * 1000 + tvrecv->tv_usec / 1000;     /*以毫秒为单位计算rtt*/
		/*显示相关信息*/
		fprintf(stderr, "%d byte from %s: icmp_seq=%u ttl=%d rtt=%.3f ms\n",
			len,
			inet_ntoa(from->sin_addr ),
			icmp->icmp_seq,
			ip->ip_ttl,
			rtt );
        fflush(stderr);
	}
    else 
        return(-1);
}

void ping(int sockfd, struct sockaddr_in *dest_addr)
{
	int packetsize;
    int recvlen;
    struct sockaddr_in from;
    int fromlen;
    struct timeval tvrecv;
    char sendpacket[PACKET_SIZE];
    char recvpacket[PACKET_SIZE];
    
    /*设置ICMP报头*/
    packetsize = pack(1, sendpacket);

    /* 发送ICMP报文 */
    if (sendto(sockfd, sendpacket, packetsize, 0,
			     (struct sockaddr *)dest_addr, sizeof(*dest_addr)) < 0 ) {
			perror( "sendto error" );
            return;
	}
    
    /* 接收ICMP报文 */
    fromlen = sizeof(from);
    recvlen = recvfrom(sockfd, recvpacket, sizeof(recvpacket), 0,
				    (struct sockaddr *) &from, &fromlen);
    if (recvlen < 0) {
        perror("Receive message failed");
        return;
    }
    
    gettimeofday(&tvrecv, NULL );  /*记录接收时间*/
    if (unpack(&from, recvpacket, recvlen, &tvrecv) == -1 )
        return;
}


main( int argc, char *argv[] )
{
	struct hostent *host;
	struct protoent *protocol;
    struct sockaddr_in dest_addr;
	unsigned long inaddr = 0l;
	int	waittime = MAX_WAIT_TIME;
	int	size = 50 * 1024;
    int sockfd;
	
    if ( argc < 2 ){
		printf( "usage:%s hostname/IP address\n", argv[0] );
		return -1;
	}

	if ( (protocol = getprotobyname( "icmp" ) ) == NULL ) {
		perror( "getprotobyname" );
		return -1;
	}
	
    /*生成使用ICMP的原始套接字,这种套接字只有root才能生成*/
	if ( (sockfd = socket(AF_INET, SOCK_RAW, protocol->p_proto ) ) < 0 ) {
		perror( "socket error" );
		return -1;
	}
	/* 回收root权限,设置当前用户权限*/
	setuid( getuid() );

	/*扩大套接字接收缓冲区到50K这样做主要为了减小接收缓冲区溢出的
	 * 的可能性,若无意中ping一个广播地址或多播地址,将会引来大量应答*/
	setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, &size, sizeof(size) );
	memset( &dest_addr, 0, sizeof(dest_addr));
	dest_addr.sin_family = AF_INET;

    /*判断是主机名还是ip地址*/
	if ( inaddr = inet_addr(argv[1]) == INADDR_NONE ) {
        
        /*是主机名*/
		if ( (host = gethostbyname( argv[1] ) ) == NULL) {
			perror( "gethostbyname error" );
			return -1;
		}
		memcpy( (char *) &dest_addr.sin_addr, host->h_addr, host->h_length );
	}
    else {
        /*是ip地址*/
		memcpy( (char *) &dest_addr, (char *) &inaddr, host->h_length );
    }
	
    /*获取main的进程id,用于设置ICMP的标志符*/
	pid = getpid();
	printf( "PING %s(%s): %d bytes data in ICMP packets.\n", argv[1],
		inet_ntoa( dest_addr.sin_addr ), ICMP_DATA_LEN );
	
    
    ping(sockfd, &dest_addr);                                                  /*发送所有ICMP报文*/
	
    close(sockfd);
	return(0);
}
