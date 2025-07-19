import { render, screen } from '@testing-library/react';
import App from './App';

test('renders AI Activity Tracker title', () => {
  render(<App />);
  const titleElement = screen.getByText(/AI Activity Tracker/i);
  expect(titleElement).toBeInTheDocument();
});
