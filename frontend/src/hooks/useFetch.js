import { useCallback, useEffect, useState } from 'react';
import api, { errorMessage } from '../api/client.js';

/** Loads data from an API path and exposes loading / error / reload. */
export default function useFetch(path) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setData((await api.get(path)).data);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [path]);

  useEffect(() => { load(); }, [load]);

  return { data, loading, error, reload: load, setData };
}
