import React, { useState } from 'react';
import { ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';
import { Button } from '../../../components/Button';
import { Modal } from '../../../components/Modal';
import { RegisterPage } from '../../auth/pages/RegisterPage';

export const LandingPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const closeModal = () => {
    setIsModalOpen(false);
  };

  return (
    <div className="max-w-4xl w-full text-center space-y-8">
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary/5 rounded-full blur-3xl -z-10 animate-pulse" />
      <div className="absolute bottom-1/4 right-1/4 w-64 h-64 bg-secondary/5 rounded-full blur-3xl -z-10 animate-pulse delay-700" />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <span className="font-label text-[0.6875rem] text-primary tracking-[0.2em] uppercase font-bold mb-4 block">
          The Future of Microservices
        </span>
        <h1 className="font-headline text-[3.5rem] md:text-[5rem] leading-[1.1] text-on-background tracking-tighter mb-6">
          Crafting Excellence <br />
          <span className="text-primary italic">In Every Byte.</span>
        </h1>
        <p className="text-on-surface-variant font-body text-lg md:text-xl max-w-2xl mx-auto leading-relaxed">
          Join the professional ecosystem designed for modern developers. 
          Scale your ideas with the precision of a digital atelier.
        </p>
      </motion.div>

      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.2 }}
        className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4"
      >
        <Button 
          onClick={() => setIsModalOpen(true)}
          size="xl"
          className="flex items-center gap-3"
        >
          Get Started <ArrowRight size={18} />
        </Button>
        <Button variant="outline" size="xl">
          Learn More
        </Button>
      </motion.div>

      {/* Features Preview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-24 max-w-6xl w-full">
        {[
          { title: 'Precision', desc: 'Every component is crafted with meticulous attention to detail.' },
          { title: 'Scale', desc: 'Built to handle millions of requests with zero latency.' },
          { title: 'Security', desc: 'Enterprise-grade protection for your most sensitive data.' }
        ].map((feature, i) => (
          <motion.div 
            key={i}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 + (i * 0.1) }}
            className="p-8 bg-white/50 backdrop-blur-sm border border-slate-200/50 rounded-2xl hover:border-primary/30 transition-colors"
          >
            <h3 className="font-headline text-xl mb-3">{feature.title}</h3>
            <p className="text-on-surface-variant text-sm leading-relaxed text-left">{feature.desc}</p>
          </motion.div>
        ))}
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title="Create Account"
        description="Join the professional microservice ecosystem."
      >
        <RegisterPage />
      </Modal>
    </div>
  );
};
