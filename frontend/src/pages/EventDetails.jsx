import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getEvent } from '../services/eventService';
import { joinEventRoom, leaveEventRoom, onSeatUpdate, disconnectSocket } from '../services/socketService';

export default function EventDetails() {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [seatUpdates, setSeatUpdates] = useState({});

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        const result = await getEvent(id);
        if (result.success) {
          setEvent(result.data);
        } else {
          setError(result.message || 'Event not found.');
        }
      } catch (err) {
        setError(
          err.response?.data?.message || 'Unable to load event details.'
        );
      } finally {
        setLoading(false);
      }
    };
    fetchEvent();
  }, [id]);

  // Real-time seat updates via Socket.io
  useEffect(() => {
    if (!event) return;

    joinEventRoom(id);

    const unsubscribe = onSeatUpdate((data) => {
      // data: { seatId, status }
      setSeatUpdates((prev) => ({
        ...prev,
        [data.seatId]: data.status,
      }));
    });

    return () => {
      leaveEventRoom(id);
      unsubscribe();
      disconnectSocket();
    };
  }, [id, event]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-32">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-stone-800 border-t-amber-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center sm:px-6 lg:px-8">
        <div className="rounded-lg bg-rose-900/20 px-4 py-3 text-sm text-rose-400 border border-rose-900/50">{error}</div>
        <Link to="/events" className="btn-ghost mt-6">
          ← Back to Events
        </Link>
      </div>
    );
  }

  if (!event) return null;

  const formattedDate = event.eventDate
    ? new Date(event.eventDate).toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      })
    : 'TBA';

  const statusColor = {
    UPCOMING: 'bg-emerald-100 text-emerald-700',
    ONGOING: 'bg-blue-100 text-blue-700',
    COMPLETED: 'bg-slate-100 text-slate-600',
    CANCELLED: 'bg-red-100 text-red-700',
  };

  // Count live seat updates by status
  const seatStatusCounts = Object.values(seatUpdates).reduce(
    (acc, status) => {
      acc[status] = (acc[status] || 0) + 1;
      return acc;
    },
    {}
  );

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8 animate-fade-in">
      <Link to="/events" className="mb-6 inline-flex items-center gap-1 text-sm font-medium text-stone-400 hover:text-amber-500 transition-colors">
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
        </svg>
        Back to Events
      </Link>

      <div className="glass-card overflow-hidden rounded-2xl">
        {/* Gradient header */}
        <div className="h-3 bg-gradient-to-r from-amber-600 to-amber-400 glow-primary" />

        <div className="p-6 sm:p-8">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <h1 className="text-2xl font-bold text-stone-100 sm:text-3xl font-[Montserrat]">{event.title}</h1>
            {event.status && (
              <span className={`rounded-full px-3 py-1 text-sm font-medium ${statusColor[event.status] || 'bg-slate-100 text-slate-600'}`}>
                {event.status}
              </span>
            )}
          </div>

          {event.description && (
            <p className="mt-4 leading-relaxed text-stone-400">{event.description}</p>
          )}

          {/* Meta grid */}
          <div className="mt-8 grid gap-4 rounded-xl bg-stone-800/50 p-5 sm:grid-cols-2 lg:grid-cols-3 border border-stone-700">
            <MetaItem
              label="Date"
              value={formattedDate}
              icon={
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                </svg>
              }
            />
            {event.eventTime && (
              <MetaItem
                label="Time"
                value={event.eventTime}
                icon={
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                }
              />
            )}
            {event.venueName && (
              <MetaItem
                label="Venue"
                value={event.venueName}
                icon={
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 0115 0z" />
                  </svg>
                }
              />
            )}
            {event.organiserName && (
              <MetaItem
                label="Organiser"
                value={event.organiserName}
                icon={
                  <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                  </svg>
                }
              />
            )}
          </div>

          {/* Real-time seat availability indicator */}
          {Object.keys(seatUpdates).length > 0 && (
            <div className="mt-6 rounded-xl border border-emerald-200 bg-emerald-50 p-4">
              <div className="flex items-center gap-2 text-sm font-medium text-emerald-800">
                <span className="relative flex h-2.5 w-2.5">
                  <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
                </span>
                Live Seat Updates
              </div>
              <div className="mt-2 flex flex-wrap gap-3 text-xs text-emerald-700">
                {seatStatusCounts.AVAILABLE && (
                  <span className="rounded-full bg-emerald-100 px-2.5 py-1 font-medium">
                    {seatStatusCounts.AVAILABLE} Available
                  </span>
                )}
                {seatStatusCounts.HELD && (
                  <span className="rounded-full bg-amber-100 px-2.5 py-1 font-medium text-amber-700">
                    {seatStatusCounts.HELD} Held
                  </span>
                )}
                {seatStatusCounts.BOOKED && (
                  <span className="rounded-full bg-slate-200 px-2.5 py-1 font-medium text-slate-600">
                    {seatStatusCounts.BOOKED} Booked
                  </span>
                )}
              </div>
            </div>
          )}

          {/* Pricing table */}
          {event.prices && event.prices.length > 0 && (
            <div className="mt-8">
              <h2 className="mb-4 text-lg font-semibold text-stone-100 font-[Montserrat]">Ticket Pricing</h2>
              <div className="overflow-hidden rounded-xl border border-stone-700">
                <table className="w-full text-left text-sm">
                  <thead className="bg-stone-800">
                    <tr>
                      <th className="px-5 py-3 font-semibold text-stone-300">Category</th>
                      <th className="px-5 py-3 text-right font-semibold text-stone-300">Price</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-stone-700 bg-stone-900/50">
                    {event.prices.map((tier, index) => (
                      <tr key={index}>
                        <td className="px-5 py-3 text-stone-300">{tier.category}</td>
                        <td className="px-5 py-3 text-right font-semibold text-amber-500">
                          ${tier.price.toFixed(2)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function MetaItem({ label, value, icon }) {
  return (
    <div className="flex items-start gap-3">
      <div className="mt-0.5 text-amber-500">{icon}</div>
      <div>
        <p className="text-xs font-medium text-stone-500">{label}</p>
        <p className="text-sm font-semibold text-stone-200">{value}</p>
      </div>
    </div>
  );
}
