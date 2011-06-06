#include "SocketStream.h"
#include <cassert>
#include <glib.h>

SocketStream::SocketStream()
: mRecvdLen(0)
{
    mRecvBuf = new char[default_recv_buffer_size + 1];
    mRecvBufSize = default_recv_buffer_size;

    GError *err = NULL;
    socket = g_socket_new(G_SOCKET_FAMILY_IPV4, G_SOCKET_TYPE_STREAM, G_SOCKET_PROTOCOL_TCP, &err);
    if (err)
    {
      socket = NULL;
      g_warning("Could not create a socket. This is a fatal error and communication is available. Error message:\n%s", err->message);
      g_error_free (err);
    }
}

SocketStream::~SocketStream()
{
  if(socket != NULL)
  {
    g_socket_close(socket, NULL);
  }
  delete [] mRecvBuf;
}

bool SocketStream::connect(const std::string& host, int port)
{
  if(socket != NULL)
  {
	gboolean conn = false;
    GError** error = NULL;
	GCancellable* cancellable = NULL;
	GSocketAddress* sockaddr = NULL;
	GError* conn_error = NULL;

	GSocketConnectable* addr = g_network_address_new(host.c_str(), port);
	GSocketAddressEnumerator* enumerator = g_socket_connectable_enumerate(addr);
	g_object_unref(addr);

	while (!conn && (sockaddr = g_socket_address_enumerator_next(enumerator, cancellable, error)))
    {
		conn = g_socket_connect(socket, sockaddr, NULL, conn_error ? NULL : &conn_error);
		g_object_unref(sockaddr);
    }
	g_object_unref(enumerator);
	
	if (conn)
    {
      return true;
    }
	else if (error)
    {
		if (conn_error){
			g_warning("Could not connect. Error message:\n%s", conn_error->message);
			g_error_free(conn_error);
		}
      return false;
    }
	else
    {
      g_propagate_error(error, conn_error);
      return false;
    }
  }

  return false;
}

void SocketStream::send(const std::string& msg)
{
  if(socket == NULL)
  {
    return;
  }
  
  if(g_socket_is_connected(socket))
  {
    GError* err = NULL;
    g_socket_send(socket, msg.c_str(), msg.size(), NULL, &err);
    if(err)
    {
      g_warning("Could not send message. Error message:\n%s", err->message);
      g_error_free (err);
      assert(false);
    }
  }
  else
  {
    g_warning("Can't send, not connected");
    assert(false);
  }
}

SocketStream& SocketStream::send()
{
  send(mSendStr.str());
  mSendStr.str("");
  return *this;
}

int SocketStream::recv(std::string& msg)
{
  if(socket == NULL)
  {
    return -1;
  }
  else if(!g_socket_is_connected(socket))
  {
    g_warning("Can not receive, socket is not connected");
    return -1;
  }

  memset(mRecvBuf, 0, mRecvBufSize + 1);
  GError* err = NULL;
  int status = g_socket_receive(socket, mRecvBuf, mRecvBufSize, NULL, &err);
  if (err)
  {
    std::cerr << "status == -1   error message: " << err->message << std::endl;
    g_error_free(err);
  }
  else if (status > 0) {
    msg = mRecvBuf;
  } else
  {
    msg = "";
  }
  return status;
}

void SocketStream::prefixedSend()
{
  if (!mSendStr.str().empty()) {
    // prefix the message with it's payload length
    unsigned int len = static_cast<unsigned int> (g_htonl(mSendStr.str().size()));
    static char preChar[sizeof (unsigned int) ];
    memcpy(preChar, &len, sizeof (unsigned int));
    std::string msg = mSendStr.str();
    msg.insert(0, preChar, sizeof (unsigned int));
    mSendStr.str() = std::string(preChar) + mSendStr.str();
    send(msg);
    mSendStr.str("");
  }
}

bool SocketStream::isFixedLengthDataAvailable(unsigned int len)
{
  if(socket == NULL || !g_socket_is_connected(socket))
  {
    return false;
  }

  if (len == 0) return true;

  /* check whether the read_buffer is large enough to handle this request
  if not, reallocate the array */
  if (mRecvBufSize < len) {
    reallocRecvBuffer(len);
  }

  for (;;) {
    /* See if we have enough data to satisfy request */
    if (mRecvdLen >= len) return true;
    /* there was not enough data in the read buffer, so let's try to get some more */
    int res = g_socket_receive(socket, mRecvBuf + mRecvdLen, mRecvBufSize - mRecvdLen, NULL, NULL);
    
    if (res <= 0) return false;
    /* res is > 0 */
    mRecvdLen += res;
  }
}

int SocketStream::prefixedRecv(std::string& msg)
{
  msg = "";
  unsigned int messLen;

  bool res = isFixedLengthDataAvailable(sizeof (unsigned int));
  if (!res) return 0;
  //get the message length
  messLen = g_ntohl((*(unsigned int*) mRecvBuf));

  //try and get the data
  res = isFixedLengthDataAvailable(messLen + sizeof (unsigned int));
  if (!res) return 0;

  //we have to copy, skipping the sizeof(int) bytes at the beginning
  msg.insert(0, mRecvBuf + sizeof (unsigned int), messLen);
  mRecvdLen -= (sizeof (unsigned int) +messLen);
  memmove(mRecvBuf, mRecvBuf + sizeof (unsigned int) +messLen, mRecvdLen);
  return messLen;
}

void SocketStream::reallocRecvBuffer(unsigned int size)
{
  char* oldBuffer = mRecvBuf;
  mRecvBuf = new char[size + 1];
  memcpy(mRecvBuf, oldBuffer, mRecvdLen);
  mRecvBufSize = size;
  delete [] oldBuffer;
}

