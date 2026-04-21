using System.Collections.Concurrent;

namespace gazelkaTest.Services
{
    public class ChatPresenceService
    {
        // userId -> chatId
        private readonly ConcurrentDictionary<int, int> _activeChats = new();

        public void EnterChat(int userId, int chatId)
        {
            _activeChats[userId] = chatId;
        }

        public void LeaveChat(int userId)
        {
            _activeChats.TryRemove(userId, out _);
        }

        public bool IsUserInChat(int userId, int chatId)
        {
            return _activeChats.TryGetValue(userId, out var activeChatId)
                   && activeChatId == chatId;
        }
    }

}
