import React, { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import { CheckCircle, Calendar, MapPin, Ticket, ArrowRight, User, CreditCard } from 'lucide-react';
import api from '../services/api';

export default function BookingSuccess() {
    const { reference } = useParams();
    const location = useLocation();
    
    // We try to get data from navigation state, if not we fetch it
    const [bookingData, setBookingData] = useState(location.state?.bookingData || null);
    const [loading, setLoading] = useState(!bookingData);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!bookingData) {
            // Wait, we need the internal ID to fetch details. If we don't have state, we might just fetch the history and find it.
            // Since we don't have a direct /api/bookings/ref endpoint, we rely on state.
            setError("Booking details not available in this session. Check your email or Booking History.");
            setLoading(false);
        }
    }, [bookingData]);

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-500"></div>
            </div>
        );
    }

    if (error && !bookingData) {
        return (
            <div className="max-w-2xl mx-auto text-center py-16 space-y-6">
                <CheckCircle className="mx-auto h-20 w-20 text-emerald-500 drop-shadow-[0_0_15px_rgba(16,185,129,0.3)]" />
                <h1 className="text-3xl font-bold text-stone-100 font-[Montserrat]">Booking Confirmed!</h1>
                <p className="text-stone-400">{error}</p>
                <div className="pt-6">
                    <Link to="/bookings" className="inline-flex items-center gap-2 btn-glow">
                        View My Bookings <ArrowRight className="w-4 h-4" />
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-3xl mx-auto space-y-8 animate-fade-in pb-12">
            <div className="text-center space-y-4 py-8">
                <div className="inline-flex items-center justify-center w-20 h-20 bg-emerald-900/30 rounded-full mb-2 border border-emerald-500/20 shadow-[0_0_30px_rgba(16,185,129,0.2)]">
                    <CheckCircle className="h-10 w-10 text-emerald-500" />
                </div>
                <h1 className="text-3xl font-bold text-stone-100 font-[Montserrat]">Booking Confirmed!</h1>
                <p className="text-lg text-stone-400 max-w-lg mx-auto">
                    Your tickets have been successfully booked. We've sent a confirmation email with your QR code to your registered email address.
                </p>
            </div>

            <div className="glass-card rounded-3xl overflow-hidden relative border border-amber-900/20">
                {/* Ticket cutouts */}
                <div className="absolute left-0 top-1/2 -mt-4 -ml-4 w-8 h-8 bg-stone-950 rounded-full border-r border-amber-900/20"></div>
                <div className="absolute right-0 top-1/2 -mt-4 -mr-4 w-8 h-8 bg-stone-950 rounded-full border-l border-amber-900/20"></div>
                
                <div className="p-8 border-b border-dashed border-stone-700">
                    <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-4 mb-8">
                        <div>
                            <p className="text-sm font-semibold text-stone-500 uppercase tracking-wider mb-1">Booking Reference</p>
                            <p className="text-2xl font-bold text-amber-500 font-mono tracking-wide glow-accent-text">{bookingData.bookingReference}</p>
                        </div>
                        <div className="sm:text-right">
                            <span className="inline-block px-4 py-1.5 bg-emerald-900/40 border border-emerald-500/30 text-emerald-400 font-semibold rounded-full text-sm">
                                {bookingData.status}
                            </span>
                        </div>
                    </div>

                    <h2 className="text-2xl font-bold text-stone-100 mb-6 font-[Montserrat]">{bookingData.eventTitle}</h2>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                        <div className="space-y-4">
                            <div className="flex items-start gap-3">
                                <Ticket className="w-5 h-5 text-amber-500 mt-0.5 shrink-0" />
                                <div>
                                    <p className="text-sm text-stone-400 mb-1">Seats</p>
                                    <p className="font-semibold text-stone-200">{bookingData.bookedSeats?.join(', ')}</p>
                                </div>
                            </div>
                        </div>
                        <div className="space-y-4">
                            <div className="flex items-start gap-3">
                                <CreditCard className="w-5 h-5 text-amber-500 mt-0.5 shrink-0" />
                                <div>
                                    <p className="text-sm text-stone-400 mb-1">Total Amount Paid</p>
                                    <p className="font-semibold text-stone-200">${bookingData.totalAmount?.toFixed(2)}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="bg-stone-900/60 p-8 flex flex-col sm:flex-row justify-between items-center gap-6">
                    <p className="text-stone-400 text-sm text-center sm:text-left">
                        Please present your email QR code at the venue.
                    </p>
                    <div className="flex gap-4 w-full sm:w-auto">
                        <Link to="/bookings" className="btn-ghost flex-1 sm:flex-none text-center">
                            My Bookings
                        </Link>
                        <Link to="/" className="btn-glow flex-1 sm:flex-none text-center">
                            Browse More
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
}
