import { Link } from "react-router-dom";
import { useTitle } from "../hooks/useTitle";
import {
  ArrowRight,
  Shield,
  Clock,
  Award,
  Users,
  Microscope,
  Heart,
  Building2,
  CheckCircle,
  Star,
  ChevronRight,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";

const stats = [
  { value: "150+", label: "Diagnostic Centers", icon: Building2 },
  { value: "500+", label: "Tests Available", icon: Microscope },
  { value: "50K+", label: "Happy Patients", icon: Users },
  { value: "99%", label: "Accuracy Rate", icon: Award },
];

const features = [
  {
    icon: Shield,
    title: "Trusted & Certified",
    desc: "All our diagnostic centers are certified and follow strict quality standards for accurate results.",
    color: "bg-blue-50 text-blue-600",
  },
  {
    icon: Clock,
    title: "Quick Turnaround",
    desc: "Get your test results promptly. Track your appointment status in real time.",
    color: "bg-indigo-50 text-indigo-600",
  },
  {
    icon: Heart,
    title: "Patient First",
    desc: "Personalized care experience from booking to receiving your results — everything at your fingertips.",
    color: "bg-pink-50 text-pink-600",
  },
];

const steps = [
  {
    step: "01",
    title: "Create Account",
    desc: "Sign up and complete your patient profile in minutes.",
  },
  {
    step: "02",
    title: "Choose a Center",
    desc: "Browse certified diagnostic centers near you.",
  },
  {
    step: "03",
    title: "Select Tests",
    desc: "Pick the diagnostic tests you need from a wide catalog.",
  },
  {
    step: "04",
    title: "Get Results",
    desc: "Track status and receive your results securely online.",
  },
];

const testimonials = [
  {
    name: "Sarah Johnson",
    role: "Patient",
    text: "The booking process was seamless. I got my blood test results within hours. Excellent service!",
    rating: 5,
  },
  {
    name: "Michael Chen",
    role: "Patient",
    text: "Found a great diagnostic center near me with all the tests I needed. Highly recommend MediCare.",
    rating: 5,
  },
  {
    name: "Priya Sharma",
    role: "Patient",
    text: "Very convenient! The appointment tracking feature kept me informed every step of the way.",
    rating: 5,
  },
];

export default function HomePage() {
  useTitle("Home");
  const { user, isAdmin, isCenterAdmin, isStaffAdmin } = useAuth();

  return (
    <div className="overflow-x-hidden">
      {/* ─── Hero ─────────────────────────────────────────────────────────── */}
      <section className="relative bg-gradient-to-br from-blue-700 via-blue-600 to-indigo-700 text-white overflow-hidden">
        {/* Decorative circles */}
        <div className="absolute -top-24 -right-24 w-96 h-96 bg-white/5 rounded-full" />
        <div className="absolute -bottom-16 -left-16 w-72 h-72 bg-white/5 rounded-full" />
        <div className="absolute top-1/2 right-1/4 w-32 h-32 bg-white/5 rounded-full" />

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 lg:py-28">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div>
              {/* Badge */}
              <div className="inline-flex items-center gap-2 bg-white/10 border border-white/20 rounded-full px-4 py-1.5 text-sm mb-6">
                <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse" />
                Expert Healthcare Diagnostics
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold leading-tight mb-6">
                Modern Care for Your{" "}
                <span className="text-blue-200">Health &amp;</span>{" "}
                <span className="text-cyan-300">Wellness</span>
              </h1>

              <p className="text-lg text-blue-100 mb-8 leading-relaxed max-w-lg">
                Book diagnostic tests at certified centers near you. Track your
                appointments, receive results, and manage your health — all in
                one place.
              </p>

              <div className="flex flex-wrap gap-4">
                {isAdmin ? (
                  <Link
                    to="/admin"
                    className="btn-primary bg-white text-blue-700 hover:bg-blue-50 flex items-center gap-2"
                  >
                    View Analytics <ArrowRight className="w-4 h-4" />
                  </Link>
                ) : isCenterAdmin || isStaffAdmin ? (
                  <>
                    <Link
                      to="/admin"
                      className="btn-primary bg-white text-blue-700 hover:bg-blue-50 flex items-center gap-2"
                    >
                      View Dashboard <ArrowRight className="w-4 h-4" />
                    </Link>
                    <Link
                      to="/admin/appointments"
                      className="btn-outline border-white text-white hover:bg-white hover:text-blue-700"
                    >
                      Manage Appointments
                    </Link>
                  </>
                ) : user ? (
                  <>
                    <Link
                      to="/book"
                      className="btn-primary bg-white text-blue-700 hover:bg-blue-50 flex items-center gap-2"
                    >
                      Book Appointment <ArrowRight className="w-4 h-4" />
                    </Link>
                    <Link
                      to="/appointments"
                      className="btn-outline border-white text-white hover:bg-white hover:text-blue-700"
                    >
                      My Appointments
                    </Link>
                  </>
                ) : (
                  <>
                    <Link
                      to="/signup"
                      className="btn-primary bg-white text-blue-700 hover:bg-blue-50 flex items-center gap-2"
                    >
                      Get Started <ArrowRight className="w-4 h-4" />
                    </Link>
                    <Link
                      to="/centers"
                      className="btn-outline border-white text-white hover:bg-white hover:text-blue-700"
                    >
                      Explore Centers
                    </Link>
                  </>
                )}
              </div>

              {/* Mini stats */}
              <div className="flex gap-6 mt-10 pt-8 border-t border-white/20">
                <div>
                  <div className="text-3xl font-extrabold text-white">50K+</div>
                  <div className="text-blue-200 text-sm">Happy Patients</div>
                </div>
                <div className="w-px bg-white/20" />
                <div>
                  <div className="text-3xl font-extrabold text-white">150+</div>
                  <div className="text-blue-200 text-sm">Certified Centers</div>
                </div>
                <div className="w-px bg-white/20" />
                <div>
                  <div className="text-3xl font-extrabold text-white">500+</div>
                  <div className="text-blue-200 text-sm">Tests Available</div>
                </div>
              </div>
            </div>

            {/* Hero card panel */}
            <div className="hidden lg:flex justify-end">
              <div className="relative w-full max-w-md">
                {/* Main card */}
                <div className="bg-white rounded-3xl shadow-2xl p-6 text-gray-800">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
                      <Microscope className="w-6 h-6 text-blue-600" />
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900">
                        Appointment Booked
                      </p>
                      <p className="text-xs text-gray-500">
                        Blood Panel — City Lab
                      </p>
                    </div>
                    <span className="ml-auto badge-approved">Approved</span>
                  </div>

                  <div className="space-y-3">
                    {[
                      "Complete Blood Count",
                      "Lipid Profile",
                      "Blood Glucose",
                    ].map((test) => (
                      <div
                        key={test}
                        className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl"
                      >
                        <CheckCircle className="w-4 h-4 text-green-500 shrink-0" />
                        <span className="text-sm text-gray-700">{test}</span>
                      </div>
                    ))}
                  </div>

                  <div className="mt-5 p-4 bg-blue-600 rounded-2xl text-white text-sm flex items-center justify-between">
                    <span>Health is Continuously Monitored</span>
                    <Heart className="w-5 h-5 text-blue-200" />
                  </div>
                </div>

                {/* Floating badge */}
                <div className="absolute -top-4 -right-4 bg-white rounded-2xl shadow-xl p-3 flex items-center gap-2">
                  <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                    <Shield className="w-4 h-4 text-green-600" />
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-gray-900">
                      Certified Centers
                    </p>
                    <p className="text-xs text-gray-500">All verified</p>
                  </div>
                </div>

                {/* Bottom floating stat */}
                <div className="absolute -bottom-4 -left-6 bg-white rounded-2xl shadow-xl px-4 py-3 flex items-center gap-3">
                  <div className="flex -space-x-2">
                    {["bg-blue-400", "bg-pink-400", "bg-green-400"].map(
                      (c, i) => (
                        <div
                          key={i}
                          className={`w-8 h-8 ${c} rounded-full border-2 border-white flex items-center justify-center text-white text-xs font-bold`}
                        >
                          {String.fromCharCode(65 + i)}
                        </div>
                      ),
                    )}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-gray-900">
                      1M+ Customers
                    </p>
                    <p className="text-xs text-gray-500">
                      Have felt the difference
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ─── Stats bar ────────────────────────────────────────────────────── */}
      <section className="bg-white border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {stats.map(({ value, label, icon: Icon }) => (
              <div key={label} className="flex items-center gap-4">
                <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center shrink-0">
                  <Icon className="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <div className="text-2xl font-extrabold text-gray-900">
                    {value}
                  </div>
                  <div className="text-sm text-gray-500">{label}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ─── Features ─────────────────────────────────────────────────────── */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <p className="text-blue-600 font-semibold text-sm uppercase tracking-wide mb-2">
              Why Choose Us
            </p>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900">
              Transforming Healthcare with
              <br />
              <span className="text-blue-600">Personalized Patient Care</span>
            </h2>
            <p className="mt-4 text-gray-500 max-w-2xl mx-auto">
              Our team of dedicated doctors and state-of-the-art facilities are
              revolutionizing the way healthcare is delivered.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            {features.map(({ icon: Icon, title, desc, color }) => (
              <div
                key={title}
                className="card hover:shadow-md transition-shadow group"
              >
                <div
                  className={`w-12 h-12 ${color} rounded-xl flex items-center justify-center mb-5 group-hover:scale-110 transition-transform`}
                >
                  <Icon className="w-6 h-6" />
                </div>
                <h3 className="font-bold text-gray-900 text-lg mb-2">
                  {title}
                </h3>
                <p className="text-gray-500 text-sm leading-relaxed">{desc}</p>
                <Link
                  to="/centers"
                  className="mt-4 inline-flex items-center gap-1 text-blue-600 text-sm font-medium hover:gap-2 transition-all"
                >
                  Learn More <ChevronRight className="w-4 h-4" />
                </Link>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ─── How it works ─────────────────────────────────────────────────── */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-14">
            <p className="text-blue-600 font-semibold text-sm uppercase tracking-wide mb-2">
              Simple Process
            </p>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900">
              Discover Our Simple Booking Process
            </h2>
          </div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {steps.map(({ step, title, desc }, index) => (
              <div key={step} className="relative">
                {index < steps.length - 1 && (
                  <div className="hidden lg:block absolute top-8 left-full w-full h-0.5 bg-blue-100 -translate-x-60 z-0" />
                )}
                <div className="relative z-10">
                  <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center mb-4 shadow-lg shadow-blue-200">
                    <span className="text-white font-extrabold text-lg">
                      {step}
                    </span>
                  </div>
                  <h3 className="font-bold text-gray-900 text-lg mb-2">
                    {title}
                  </h3>
                  <p className="text-gray-500 text-sm leading-relaxed">
                    {desc}
                  </p>
                </div>
              </div>
            ))}
          </div>

          <div className="text-center mt-12">
            <Link
              to={user ? "/book" : "/signup"}
              className="btn-primary inline-flex items-center gap-2"
            >
              {user ? "Book Now" : "Get Started"}{" "}
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </section>

      {/* ─── Advanced Equipment section ───────────────────────────────────── */}
      <section className="py-20 bg-gradient-to-r from-blue-700 to-indigo-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div>
              <p className="text-blue-200 font-semibold text-sm uppercase tracking-wide mb-3">
                Revolutionary
              </p>
              <h2 className="text-3xl sm:text-4xl font-extrabold mb-6">
                Providing Advanced Medical Equipment and Experienced Staff
              </h2>
              <p className="text-blue-100 mb-8 leading-relaxed">
                Our network of diagnostic centers is equipped with the latest
                medical technology to support accurate diagnosis and effective
                treatment.
              </p>

              <div className="space-y-4">
                {[
                  {
                    icon: Shield,
                    title: "State-of-the-Art Equipment",
                    desc: "Our facility is equipped with the latest medical technology to support accurate diagnosis and effective treatment.",
                  },
                  {
                    icon: Users,
                    title: "Experienced Medical Staff",
                    desc: "Our team of doctors and nurses have years of experience in their respective fields.",
                  },
                ].map(({ icon: Icon, title, desc }) => (
                  <div
                    key={title}
                    className="flex gap-4 bg-white/10 rounded-2xl p-4 border border-white/10"
                  >
                    <div className="w-10 h-10 bg-blue-500 rounded-xl flex items-center justify-center shrink-0">
                      <Icon className="w-5 h-5 text-white" />
                    </div>
                    <div>
                      <p className="font-semibold text-sm">{title}</p>
                      <p className="text-blue-200 text-xs mt-1">{desc}</p>
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-8 flex gap-4">
                <Link
                  to="/centers"
                  className="btn-primary bg-white text-blue-700 hover:bg-blue-50 flex items-center gap-2"
                >
                  Book Now <ArrowRight className="w-4 h-4" />
                </Link>
                <Link
                  to="/tests"
                  className="btn-outline border-white text-white hover:bg-white hover:text-blue-700"
                >
                  Learn More
                </Link>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              {[
                "33K+\nTrusted by many",
                "150+\nCertified Centers",
                "500+\nTests Available",
                "24/7\nOnline Support",
              ].map((text, i) => (
                <div
                  key={i}
                  className="bg-white/10 border border-white/20 rounded-2xl p-6 text-center"
                >
                  <div className="text-3xl font-extrabold text-white">
                    {text.split("\n")[0]}
                  </div>
                  <div className="text-blue-200 text-sm mt-1">
                    {text.split("\n")[1]}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* ─── Testimonials ─────────────────────────────────────────────────── */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <p className="text-blue-600 font-semibold text-sm uppercase tracking-wide mb-2">
              Testimonials
            </p>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900">
              What Our Patients Say
            </h2>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            {testimonials.map(({ name, role, text, rating }) => (
              <div
                key={name}
                className="card hover:shadow-md transition-shadow"
              >
                <div className="flex gap-1 mb-4">
                  {Array.from({ length: rating }).map((_, i) => (
                    <Star
                      key={i}
                      className="w-4 h-4 text-yellow-400 fill-yellow-400"
                    />
                  ))}
                </div>
                <p className="text-gray-600 text-sm leading-relaxed mb-5 italic">
                  "{text}"
                </p>
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold">
                    {name.charAt(0)}
                  </div>
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {name}
                    </p>
                    <p className="text-gray-400 text-xs">{role}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ─── CTA ──────────────────────────────────────────────────────────── */}
      {!user && (
        <section className="py-16 bg-white">
          <div className="max-w-3xl mx-auto px-4 text-center">
            <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900 mb-4">
              Ready to Take Control of Your Health?
            </h2>
            <p className="text-gray-500 mb-8">
              Join thousands of patients who trust MediCare for their diagnostic
              needs.
            </p>
            <div className="flex flex-wrap gap-4 justify-center">
              <Link
                to="/signup"
                className="btn-primary text-base flex items-center gap-2"
              >
                Create Free Account <ArrowRight className="w-4 h-4" />
              </Link>
              <Link to="/centers" className="btn-outline text-base">
                Browse Centers
              </Link>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
