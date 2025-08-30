// Saves options to chrome.storage
const saveOptions = () => {
  const apiEndpoint = document.getElementById('api-endpoint').value;
  const debugMode = document.getElementById('debug-mode').checked;

  chrome.storage.sync.set(
    { apiEndpoint: apiEndpoint, debugMode: debugMode },
    () => {
      // Update status to let user know options were saved.
      const status = document.getElementById('status');
      status.textContent = 'Options saved.';
      setTimeout(() => {
        status.textContent = '';
      }, 1500);
    }
  );
};

// Restores options state using the preferences stored in chrome.storage.
const restoreOptions = () => {
  // Use default values
  chrome.storage.sync.get(
    {
      apiEndpoint: 'http://localhost:11240/api/data/capture',
      debugMode: true
    },
    (items) => {
      document.getElementById('api-endpoint').value = items.apiEndpoint;
      document.getElementById('debug-mode').checked = items.debugMode;
    }
  );
};

document.addEventListener('DOMContentLoaded', restoreOptions);
document.getElementById('save').addEventListener('click', saveOptions);