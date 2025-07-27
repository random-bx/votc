import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { motion, AnimatePresence } from 'framer-motion';
import { Mic, MapPin, Loader, ServerCrash, Languages } from 'lucide-react';

const App = () => {
  const [appState, setAppState] = useState('idle'); // 'idle', 'recording', 'loading', 'error'
  const [response, setResponse] = useState(null);
  const [error, setError] = useState('');
  
  const mediaRecorderRef = useRef(null);
  const recordingTimeoutRef = useRef(null);
  const audioChunksRef = useRef([]);

  const handleMicClick = () => {
    if (appState === 'recording') {
      stopRecording();
    } else {
      startRecording();
    }
  };

  const startRecording = async () => {
    setResponse(null);
    setError('');
    setAppState('recording');
    audioChunksRef.current = [];

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream, { mimeType: 'audio/webm;codecs=opus' });
      mediaRecorderRef.current = recorder;

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      recorder.onstop = () => {
        // Clear the timeout just in case
        clearTimeout(recordingTimeoutRef.current);
        
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm;codecs=opus' });
        if (audioBlob.size > 0) {
          sendAudioToServer(audioBlob);
        } else {
          setError("I didn't hear anything. Please try again.");
          setAppState('error');
        }
        stream.getTracks().forEach(track => track.stop()); // Important: release the microphone
      };

      recorder.start();
      
      // NEW: Automatically stop recording after 10 seconds
      recordingTimeoutRef.current = setTimeout(() => {
        if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") {
            stopRecording();
        }
      }, 10000); // 10 seconds

    } catch (err) {
      console.error("Microphone error:", err);
      setError("Microphone access is required. Please allow it.");
      setAppState('error');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") {
      mediaRecorderRef.current.stop();
      // The onstop event handler will take care of the rest
    }
  };

  const sendAudioToServer = async (audioBlob) => {
    setAppState('loading');
    const formData = new FormData();
    formData.append('audio', audioBlob, 'recording.webm');

    try {
      const backendUrl = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
      const { data } = await axios.post(`${backendUrl}/api/v1/transcribe`, formData);

      if (data && data.places && data.places.length > 0) {
        setResponse(data);
        setAppState('idle');
      } else {
        setError("I couldn't find any recommendations for that. Please try again.");
        setAppState('error');
      }
    } catch (err) {
      console.error("Server error:", err);
      setError("Sorry, there was a problem connecting to the server.");
      setAppState('error');
    }
  };
  
  const StatusDisplay = () => {
    switch (appState) {
      case 'recording':
        return <p className="mt-4 text-brand-gray">Listening...</p>;
      case 'loading':
        return <p className="mt-4 text-brand-gray animate-pulse">Thinking...</p>;
      case 'error':
        return <div className="mt-6 flex items-center justify-center gap-2 text-red-500"><ServerCrash size={20} /><p>{error}</p></div>;
      default:
        return <p className="mt-4 text-brand-gray">Click the microphone and speak naturally.</p>;
    }
  };

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-gray-50 to-blue-100 flex flex-col items-center justify-center p-4 font-sans">
      <motion.main 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-2xl bg-white/70 backdrop-blur-xl rounded-2xl shadow-lg p-8"
      >
        <div className="text-center">
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 260, damping: 20, delay: 0.2 }}
            className="flex items-center justify-center gap-3"
          >
            <Languages size={36} className="text-brand-blue" />
            <h1 className="text-4xl font-bold text-brand-dark">Voice of the City</h1>
          </motion.div>
          <p className="text-md text-brand-gray mt-2">Your AI-powered local travel guide.</p>
        </div>

        <div className="my-8 flex justify-center">
          <motion.button
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            onClick={handleMicClick}
            disabled={appState === 'loading'}
            className={`relative w-24 h-24 rounded-full flex items-center justify-center transition-colors duration-300
              ${appState === 'recording' ? 'bg-red-500 text-white' : 'bg-brand-blue text-white'}
              disabled:opacity-50 disabled:cursor-not-allowed shadow-lg`}
          >
            {appState === 'recording' && <span className="absolute inset-0 m-auto w-full h-full rounded-full bg-red-500 animate-ping"></span>}
            {appState === 'loading' ? <Loader size={32} className="animate-spin" /> : <Mic size={32} />}
          </motion.button>
        </div>

        <div className="text-center h-12">
          <StatusDisplay />
        </div>

        <AnimatePresence>
          {response && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="mt-8 pt-6 border-t"
            >
              <h3 className="text-xl font-semibold text-brand-dark mb-4">Here are my recommendations:</h3>
              <div className="space-y-4">
                {response.places.map((place, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.5, delay: index * 0.1 }}
                    className="flex items-start gap-4 p-4 rounded-lg bg-brand-light-blue"
                  >
                    <div className="flex-shrink-0 w-8 h-8 rounded-full bg-brand-blue text-white flex items-center justify-center font-bold">
                      {index + 1}
                    </div>
                    <div>
                      <h4 className="font-bold text-lg text-brand-dark">{place.name}</h4>
                      <a
                        href={place.mapLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-1 text-brand-blue hover:underline text-sm"
                      >
                        <MapPin size={14} />
                        View on Google Maps
                      </a>
                    </div>
                  </motion.div>
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.main>
    </div>
  );
};

export default App;