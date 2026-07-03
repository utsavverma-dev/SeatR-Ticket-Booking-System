import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { Calendar, MapPin, Ticket, ChevronRight, XCircle } from 'lucide-react';

export default function BookingHistory() {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchBookings();
    }, []);

    const fetchBookings = async () => {
        try {
            const response = await api.get('/bookings');
            setBookings(response.data.data);
        } catch (err) {
            setError('Failed to load bookings.');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-500"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-center mt-10 bg-rose-900/20 p-6 rounded-xl border border-rose-900/50 max-w-2xl mx-auto">
                <p className="text-rose-400 font-medium">{error}</p>
            </div>
        );
    }

    if (bookings.length === 0) {
        return (
            <div className="max-w-4xl mx-auto text-center py-16 px-4 glass-card rounded-2xl">
                <Ticket className="mx-auto h-16 w-16 text-stone-700 mb-4" />
                <h2 className="text-2xl font-bold text-stone-100 mb-2 font-[Montserrat]">No Bookings Found</h2>
                <p className="text-stone-400 mb-8">You haven't booked any events yet. Discover amazing events and book your tickets today!</p>
                <Link to="/" className="inline-flex items-center gap-2 btn-glow">
                    Browse Events
                </Link>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto space-y-6 animate-fade-in pb-16">
            <h1 className="text-3xl font-bold text-stone-100 mb-8 font-[Montserrat]">My Bookings</h1>
            
            <div className="space-y-4">
                {bookings.map((booking) => (
                    <Link 
                        key={booking.id} 
                        to={`/bookings/${booking.id}`}
                        className="block glass-card rounded-2xl hover:shadow-[0_0_15px_rgba(245,158,11,0.15)] border-stone-800 hover:border-amber-500/50 transition-all p-6 group"
                    >
                        <div className="flex flex-col md:flex-row justify-between gap-6">
                            <div className="space-y-4 flex-grow">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h3 className="text-xl font-bold text-stone-100 group-hover:text-amber-500 transition-colors font-[Montserrat]">
                                            {booking.eventTitle}
                                        </h3>
                                        <p className="text-sm text-stone-500 font-mono mt-1">Ref: {booking.bookingReference}</p>
                                    </div>
                                    
                                    <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border ${
                                        booking.status === 'CONFIRMED' 
                                        ? 'bg-emerald-900/40 text-emerald-400 border-emerald-800' 
                                        : 'bg-rose-900/40 text-rose-400 border-rose-800'
                                    }`}>
                                        {booking.status}
                                    </span>
                                </div>

                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                    <div className="flex items-center gap-2 text-stone-300">
                                        <Calendar className="w-4 h-4 text-stone-500" />
                                        <span className="text-sm">{new Date(booking.eventDate).toLocaleDateString()} at {new Date(booking.eventDate).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                                    </div>
                                    <div className="flex items-center gap-2 text-stone-300">
                                        <MapPin className="w-4 h-4 text-stone-500" />
                                        <span className="text-sm">{booking.venueName}</span>
                                    </div>
                                </div>
                            </div>
                            
                            <div className="flex items-center justify-between md:flex-col md:justify-center md:items-end gap-2 md:border-l md:border-stone-700 md:pl-6 min-w-[120px]">
                                <div className="text-lg font-bold text-amber-500">${booking.totalAmount.toFixed(2)}</div>
                                <div className="text-amber-500 flex items-center text-sm font-medium">
                                    View Details <ChevronRight className="w-4 h-4 ml-1 transform group-hover:translate-x-1 transition-transform" />
                                </div>
                            </div>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
}
