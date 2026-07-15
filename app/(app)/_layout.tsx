/**
 * Layout Application — écrans authentifiés
 *
 * Les subscriptions Realtime (demandes partenaire + liste partenaires + présence)
 * sont initialisées ici pour rester actives quel que soit l'onglet ouvert.
 */
import { useEffect, useRef } from 'react';
import { Stack } from 'expo-router';
import { useYoumeColors } from '../../src/shared/constants/theme';
import { useAuthStore } from '../../src/presentation/stores/authStore';
import { usePartnerStore } from '../../src/presentation/stores/partnerStore';
import { partnerRepository } from '../../src/infrastructure/supabase/PartnerRepository';

export default function AppLayout() {
  const colors = useYoumeColors();
  const { user } = useAuthStore();
  const { partners, setPartners, setPendingRequests, updatePartnerPresence } = usePartnerStore();

  // Subscription globale — liste + demandes
  useEffect(() => {
    if (!user) return;
    const unsubPartners = partnerRepository.subscribeToPartners(user.id, setPartners);
    const unsubRequests = partnerRepository.subscribeToRequests(user.id, setPendingRequests);
    return () => { unsubPartners(); unsubRequests(); };
  }, [user?.id]);

  // Subscription présence — se reconnecte quand la liste de partenaires change
  const partnerIdsKey = partners.map((p) => p.partnerId).join(',');
  useEffect(() => {
    if (!partners.length) return;
    const ids = partners.map((p) => p.partnerId);
    const unsub = partnerRepository.subscribeToPartnersPresence(
      ids,
      (partnerId, isOnline, lastSeen) => updatePartnerPresence(partnerId, isOnline, lastSeen)
    );
    return () => unsub();
  }, [partnerIdsKey]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <Stack
      screenOptions={{
        headerShown: false,
        contentStyle: { backgroundColor: colors.background },
        animation: 'slide_from_right',
      }}
    >
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen name="chat/[id]" options={{ headerShown: false, animation: 'slide_from_right' }} />
      <Stack.Screen name="ai-insights/[id]" options={{ headerShown: false, animation: 'slide_from_bottom' }} />
      <Stack.Screen name="analysis/[id]" options={{ headerShown: false, animation: 'slide_from_bottom' }} />
      <Stack.Screen name="flags/[id]" options={{ headerShown: false, animation: 'slide_from_bottom' }} />
    </Stack>
  );
}
