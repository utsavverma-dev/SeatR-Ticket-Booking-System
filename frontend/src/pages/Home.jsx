import { Link } from 'react-router-dom';
import { useState } from 'react';
import useAuth from '../hooks/useAuth';
import api from '../services/api';

export default function Home() {
  const { user } = useAuth();

  return (
    <div>
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-stone-950 border-b border-amber-900/20">
        {/* Decorative elements */}
        <div className="absolute inset-0 opacity-20">
          <div className="absolute -right-20 -top-20 h-[500px] w-[500px] rounded-full bg-amber-600/30 blur-3xl animate-float" />
          <div className="absolute -bottom-20 -left-20 h-[500px] w-[500px] rounded-full bg-orange-700/20 blur-3xl" />
        </div>

        <div className="relative mx-auto max-w-7xl px-4 py-24 sm:px-6 sm:py-32 lg:px-8 lg:py-40">
          <div className="mx-auto max-w-2xl text-center">
            <span className="mb-4 inline-block rounded-full border border-amber-500/30 bg-amber-500/10 px-4 py-1.5 text-sm font-medium text-amber-300">
              🎫 Your gateway to premium events
            </span>
            <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-stone-100 sm:text-5xl lg:text-6xl font-[Montserrat]">
              Discover & Book
              <span className="block bg-gradient-to-r from-amber-300 to-amber-500 bg-clip-text text-transparent">
                Exclusive Experiences
              </span>
            </h1>
            <p className="mt-6 text-lg leading-relaxed text-stone-400">
              From concerts and theater to sports and conferences — find your next
              experience and book tickets in seconds.
            </p>
            <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
              <Link to="/events" className="btn-glow px-8 py-3 text-base">
                Browse Events
              </Link>
              {!user && (
                <Link to="/register" className="btn-ghost">
                  Create Account
                </Link>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 mesh-bg">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h2 className="text-4xl font-extrabold text-stone-100 tracking-tight font-[Montserrat]">Why SeatR?</h2>
            <p className="mt-3 text-lg text-stone-400">Everything you need for a seamless event experience.</p>
          </div>

          <div className="mt-14 grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
            <FeatureCard
              icon={
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                </svg>
              }
              title="Discover Events"
              description="Browse a curated selection of events — concerts, sports, theater, conferences, and more."
            />
            <FeatureCard
              icon={
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M16.5 6v.75m0 3v.75m0 3v.75m0 3V18m-9-5.25h5.25M7.5 15h3M3.375 5.25c-.621 0-1.125.504-1.125 1.125v3.026a2.999 2.999 0 010 5.198v3.026c0 .621.504 1.125 1.125 1.125h17.25c.621 0 1.125-.504 1.125-1.125v-3.026a2.999 2.999 0 010-5.198V6.375c0-.621-.504-1.125-1.125-1.125H3.375z" />
                </svg>
              }
              title="Easy Booking"
              description="Select your tickets, choose your seats, and complete your booking in just a few clicks."
            />
            <FeatureCard
              icon={
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
                </svg>
              }
              title="Secure & Trusted"
              description="Your transactions are safe with our secure platform. Book with confidence every time."
            />
          </div>
        </div>
      </section>



      {/* CTA Section */}
      <section className="bg-stone-900 border-t border-amber-900/30 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-t from-amber-900/10 to-transparent"></div>
        <div className="relative mx-auto max-w-7xl px-4 py-20 text-center sm:px-6 lg:px-8">
          <h2 className="text-3xl font-extrabold text-stone-100 tracking-tight font-[Montserrat]">Ready to find your next event?</h2>
          <p className="mt-4 text-lg text-stone-400">
            Join thousands of event-goers who trust SeatR.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link to="/events" className="btn-glow px-8 py-3 text-base shadow-[0_0_30px_rgba(217,119,6,0.2)]">
              Explore Events
            </Link>
            {!user && (
              <Link to="/register" className="btn-ghost">
                Sign Up Free
              </Link>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}



function FeatureCard({ icon, title, description }) {
  return (
    <div className="glass-card rounded-2xl p-8 hover:-translate-y-1 transition-all duration-300 group">
      <div className="mb-6 flex h-14 w-14 items-center justify-center rounded-xl bg-amber-500/10 text-amber-500 border border-amber-500/20 group-hover:scale-110 transition-transform">
        {icon}
      </div>
      <h3 className="text-xl font-bold text-stone-100 tracking-tight font-[Montserrat]">{title}</h3>
      <p className="mt-3 text-sm leading-relaxed text-stone-400">{description}</p>
    </div>
  );
}
