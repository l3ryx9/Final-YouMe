/**
 * Écran Principal — Liste des Conversations
 */
import React, { useEffect, useCallback, useMemo } from 'react';
import {
  View,
  Text,
  ImageBackground,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  RefreshControl,
  Alert,
  Platform,
} from 'react-native';
import { router } from 'expo-router';
import Animated, { FadeInUp, Layout } from 'react-native-reanimated';
import { Ionicons } from '@expo/vector-icons';
import { useYoumeColors, YoumeColors, SPACING, TYPOGRAPHY, BORDER_RADIUS } from '../../../src/shared/constants/theme';
import { Avatar } from '../../../src/presentation/components/common/Avatar';
import { formatConversationDate } from '../../../src/shared/utils/dateUtils';
import { useAuthStore } from '../../../src/presentation/stores/authStore';
import { useConversationStore } from '../../../src/presentation/stores/conversationStore';
import type { ConversationWithPartner } from '../../../src/domain/entities/Conversation';
import { supabase, TABLES } from '../../../src/infrastructure/supabase/config';

export default function ConversationsScreen() {
  const { user } = useAuthStore();
  const { conversations, setConversations, removeConversation, isLoading } = useConversationStore();
  const [refreshing, setRefreshing] = React.useState(false);
  const colors = useYoumeColors();
  const styles = useMemo(() => getStyles(colors), [colors]);

  useEffect(() => {
    if (!user) return;

    let channel: ReturnType<typeof supabase.channel> | null = null;

    const loadConversations = async () => {
      const { data: convRows, error: convRowsErr } = await supabase
        .from(TABLES.CONVERSATIONS)
        .select('*')
        .contains('participant_ids', [user.id])
        .not('last_message', 'is', null)
        .order('updated_at', { ascending: false });

      if (convRowsErr) {
        console.error('[ConversationsScreen] Erreur chargement conversations :', convRowsErr.message);
        return;
      }
      if (!convRows) return;

      const partnerIds = Array.from(
        new Set(
          convRows
            .map((row) => (row.participant_ids as string[]).find((id) => id !== user.id))
            .filter((id): id is string => !!id)
        )
      );

      const profileEntries = await Promise.all(
        partnerIds.map(async (id) => {
          try {
            const { data } = await supabase
              .from(TABLES.PUBLIC_PROFILES)
              .select('*')
              .eq('id', id)
              .single();
            return [id, data] as const;
          } catch {
            return [id, null] as const;
          }
        })
      );
      const profiles = new Map(profileEntries);

      const convs: ConversationWithPartner[] = [];
      for (const row of convRows) {
        const partnerId = (row.participant_ids as string[]).find((id) => id !== user.id);
        if (!partnerId) continue;

        const profile = profiles.get(partnerId);

        convs.push({
          id: row.id,
          participantIds: row.participant_ids as [string, string],
          lastMessage: (row.last_message as unknown as ConversationWithPartner['lastMessage']) ?? undefined,
          unreadCount: row.unread_count ?? 0,
          createdAt: row.created_at ? new Date(row.created_at) : new Date(),
          updatedAt: row.updated_at ? new Date(row.updated_at) : new Date(),
          partnerId,
          partnerUsername: profile?.username ?? 'partenaire',
          partnerDisplayName: profile?.display_name ?? 'Partenaire',
          partnerPhotoURL: profile?.photo_url ?? undefined,
          partnerIsOnline: profile?.is_online ?? false,
          partnerLastSeen: profile?.last_seen ? new Date(profile.last_seen) : new Date(),
        });
      }
      setConversations(convs);
    };

    loadConversations();

    channel = supabase
      .channel(`conversations:${user.id}`)
      .on('postgres_changes', { event: '*', schema: 'public', table: TABLES.CONVERSATIONS }, () => {
        loadConversations();
      })
      .subscribe();

    return () => {
      if (channel) supabase.removeChannel(channel);
    };
  }, [user]);

  const handleDeleteConversation = useCallback((item: ConversationWithPartner) => {
    Alert.alert(
      'Supprimer la conversation',
      `Voulez-vous supprimer la conversation avec ${item.partnerDisplayName} ?`,
      [
        {
          text: 'Supprimer',
          style: 'destructive',
          onPress: async () => {
            try {
              await supabase.from(TABLES.CONVERSATIONS).delete().eq('id', item.id);
              removeConversation(item.id);
            } catch {
              Alert.alert('Erreur', 'Impossible de supprimer la conversation.');
            }
          },
        },
        { text: 'Annuler', style: 'cancel' },
      ]
    );
  }, [removeConversation]);

  const renderItem = useCallback(
    ({ item, index }: { item: ConversationWithPartner; index: number }) => (
      <Animated.View entering={FadeInUp.delay(index * 30)} layout={Layout.springify()}>
        <TouchableOpacity
          style={styles.item}
          onPress={() => router.push(`/(app)/chat/${item.id}`)}
          onLongPress={() => handleDeleteConversation(item)}
          delayLongPress={400}
          activeOpacity={0.7}
        >
          <Avatar
            displayName={item.partnerDisplayName}
            photoURL={item.partnerPhotoURL}
            size={52}
            isOnline={item.partnerIsOnline}
          />
          <View style={styles.itemContent}>
            <View style={styles.itemHeader}>
              <Text style={styles.itemName} numberOfLines={1}>
                {item.partnerDisplayName}
              </Text>
              <Text style={styles.itemTime}>
                {item.lastMessage ? formatConversationDate(
                  item.lastMessage.createdAt instanceof Date
                    ? item.lastMessage.createdAt
                    : new Date(String(item.lastMessage.createdAt))
                ) : ''}
              </Text>
            </View>
            <View style={styles.itemFooter}>
              <Text style={styles.itemLastMessage} numberOfLines={1}>
                {item.lastMessage?.type === 'voice'
                  ? '🎤 Message vocal'
                  : item.lastMessage?.content ?? 'Commencer la conversation'}
              </Text>
              {item.unreadCount > 0 && (
                <View style={styles.unreadBadge}>
                  <Text style={styles.unreadText}>{item.unreadCount}</Text>
                </View>
              )}
            </View>
          </View>
        </TouchableOpacity>
      </Animated.View>
    ),
    [styles, handleDeleteConversation]
  );

  return (
    <ImageBackground
      source={require('../../../assets/images/logo-splash.png')}
      style={styles.container}
      imageStyle={styles.backgroundImage}
    >
      {/* ── Header YouMe avec canopée de feuillages ── */}
      <View style={styles.header}>
        {/* Rang supérieur de feuillages */}
        <View style={styles.foliageTop} pointerEvents="none">
          <Text style={styles.leaf}>🌿</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🍃</Text>
          <Text style={[styles.leaf, styles.leafMd]}>🌱</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🍃</Text>
          <Text style={styles.leaf}>🌿</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🍂</Text>
          <Text style={[styles.leaf, styles.leafMd]}>🌿</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🍃</Text>
          <Text style={styles.leaf}>🌿</Text>
        </View>

        {/* Titre YouMe — Impact bold */}
        <Text style={styles.brandTitle}>YouMe</Text>

        {/* Rang inférieur de feuillages */}
        <View style={styles.foliageBottom} pointerEvents="none">
          <Text style={[styles.leaf, styles.leafSm]}>🍃</Text>
          <Text style={styles.leaf}>🌿</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🌱</Text>
          <Text style={[styles.leaf, styles.leafMd]}>🍃</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🌿</Text>
          <Text style={[styles.leaf, styles.leafMd]}>🍂</Text>
          <Text style={[styles.leaf, styles.leafSm]}>🍃</Text>
        </View>
      </View>

      {/* Liste des conversations */}
      <FlatList
        data={conversations}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => setRefreshing(false)}
            tintColor={colors.primary}
          />
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <Ionicons name="chatbubbles-outline" size={64} color={colors.textMuted} />
            <Text style={styles.emptyTitle}>Aucune conversation</Text>
            <Text style={styles.emptySubtitle}>
              Ajoutez des partenaires pour commencer à discuter
            </Text>
          </View>
        }
        ItemSeparatorComponent={() => <View style={styles.separator} />}
      />
    </ImageBackground>
  );
}

function getStyles(colors: YoumeColors) {
  return StyleSheet.create({
    container: { flex: 1, backgroundColor: colors.background },
    backgroundImage: {
      opacity: 0.07,
      resizeMode: 'contain',
    },

    // ── Header canopée ──
    header: {
      backgroundColor: colors.secondary,
      paddingTop: 44,
      paddingBottom: 10,
      paddingHorizontal: SPACING.sm,
      alignItems: 'center',
      overflow: 'hidden',
    },
    foliageTop: {
      flexDirection: 'row',
      justifyContent: 'space-around',
      alignItems: 'flex-end',
      width: '100%',
      marginBottom: 2,
    },
    foliageBottom: {
      flexDirection: 'row',
      justifyContent: 'space-around',
      alignItems: 'flex-start',
      width: '100%',
      marginTop: 2,
    },
    leaf: {
      fontSize: 22,
      lineHeight: 26,
    },
    leafSm: {
      fontSize: 15,
      lineHeight: 20,
    },
    leafMd: {
      fontSize: 18,
      lineHeight: 22,
    },
    brandTitle: {
      fontFamily: Platform.OS === 'ios' ? 'Impact' : 'sans-serif-condensed',
      fontSize: 38,
      fontWeight: '900',
      color: colors.textPrimary,
      letterSpacing: 3,
      textTransform: 'uppercase',
      lineHeight: 44,
    },

    // ── Liste ──
    item: {
      flexDirection: 'row',
      alignItems: 'center',
      paddingHorizontal: SPACING.md,
      paddingVertical: SPACING.sm,
      gap: SPACING.md,
      backgroundColor: colors.background,
    },
    itemContent: { flex: 1 },
    itemHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
    itemName: { fontSize: TYPOGRAPHY.size.md, fontWeight: '600', color: colors.textPrimary, flex: 1 },
    itemTime: { fontSize: TYPOGRAPHY.size.xs, color: colors.textMuted, marginLeft: SPACING.sm },
    itemFooter: { flexDirection: 'row', alignItems: 'center', marginTop: 3 },
    itemLastMessage: { flex: 1, fontSize: TYPOGRAPHY.size.sm, color: colors.textSecondary },
    unreadBadge: {
      backgroundColor: colors.primary,
      borderRadius: 10,
      minWidth: 20,
      height: 20,
      justifyContent: 'center',
      alignItems: 'center',
      paddingHorizontal: 4,
    },
    unreadText: { fontSize: TYPOGRAPHY.size.xs, color: '#FFFFFF', fontWeight: '700' },
    separator: { height: 1, backgroundColor: colors.divider, marginLeft: 80 },
    empty: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingTop: 80, gap: SPACING.md },
    emptyTitle: { fontSize: TYPOGRAPHY.size.lg, color: colors.textSecondary, fontWeight: '600' },
    emptySubtitle: { fontSize: TYPOGRAPHY.size.sm, color: colors.textMuted, textAlign: 'center' },
  });
}
