import { io } from 'socket.io-client';

const SOCKET_URL = 'http://localhost:8080';

let socket = null;

export const getSocket = () => {
  if (!socket) {
    socket = io(SOCKET_URL, {
      autoConnect: false,
      transports: ['websocket', 'polling'],
    });
  }
  return socket;
};

export const joinEventRoom = (eventId) => {
  const s = getSocket();
  if (!s.connected) {
    s.connect();
  }
  s.emit('join_event', eventId);
  return s;
};

export const leaveEventRoom = (eventId) => {
  const s = getSocket();
  if (s.connected) {
    s.emit('leave_event', eventId);
  }
};

export const onSeatUpdate = (callback) => {
  const s = getSocket();
  s.on('seat_update', callback);
  return () => s.off('seat_update', callback);
};

export const disconnectSocket = () => {
  if (socket && socket.connected) {
    socket.disconnect();
  }
};
