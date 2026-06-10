// eslint-disable-next-line @typescript-eslint/no-require-imports

'use client';

import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';

interface WebSocketMessage {
  type: string;
  payload: unknown;
}

export function useWebSocket() {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState<WebSocketMessage[]>([]);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      brokerURL: undefined,
      webSocketFactory: () => {
        const SockJS = require('sockjs-client');
        return new SockJS(
          `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/ws`
        );
      },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/orders', (message) => {
          try {
            const payload = JSON.parse(message.body);
            setMessages(prev => [
              ...prev,
              { type: 'ORDER_UPDATE', payload },
            ]);
          } catch (e) {
            console.error('Failed to parse WebSocket message', e);
          }
        });
      },
      onDisconnect: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  return { connected, messages };
}