import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ProductSelection from './pages/ProductSelection';
import BudgetSelection from './pages/BudgetSelection';
import PrioritySelection from './pages/PrioritySelection';
import Recommendations from './pages/Recommendations';
import TestConnection from './pages/TestConnection';
import ThemeToggle from './components/ThemeToggle'; 

function App() {
  return (
    <Router>
      <ThemeToggle />
      <Routes>
        <Route path="/" element={<ProductSelection />} />
        <Route path="/budget" element={<BudgetSelection />} />
        <Route path="/priorities" element={<PrioritySelection />} />
        <Route path="/recommendations" element={<Recommendations />} />
        <Route path="/test" element={<TestConnection />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
