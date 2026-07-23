import random
import uuid
import datetime
import pandas as pd

# 1. Khởi tạo Seed Data (User & Post) theo Checklist
personas_config = {
    "Sneakerhead": {"search": 0.4, "like": 0.6, "save": 0.3, "comment": 0.25, "follow": 0.2, "add_cart": 0.15, "purchase": 0.05},
    "Beauty Lover": {"search": 0.5, "like": 0.5, "save": 0.6, "comment": 0.40, "follow": 0.3, "add_cart": 0.20, "purchase": 0.08},
    "Tech & Gaming": {"search": 0.7, "like": 0.2, "save": 0.5, "comment": 0.10, "follow": 0.15, "add_cart": 0.10, "purchase": 0.03},
    "Fashion & Custom": {"search": 0.4, "like": 0.8, "save": 0.7, "comment": 0.15, "follow": 0.4, "add_cart": 0.25, "purchase": 0.10},
    "Travel Explorer": {"search": 0.8, "like": 0.4, "save": 0.8, "comment": 0.20, "follow": 0.5, "add_cart": 0.05, "purchase": 0.01},
    "Casual Scroller": {"search": 0.1, "like": 0.05, "save": 0.02, "comment": 0.01, "follow": 0.01, "add_cart": 0.01, "purchase": 0.0},
    "Graphic Apparel Lover": {"search": 0.5, "like": 0.7, "save": 0.6, "comment": 0.1, "follow": 0.4, "add_cart": 0.25, "purchase": 0.08},
    "Impulse Shopper": {"search": 0.1, "like": 0.6, "save": 0.2, "comment": 0.05, "follow": 0.15, "add_cart": 0.6, "purchase": 0.3},
    "Trend Scraper": {"search": 0.7, "like": 0.3, "save": 0.8, "comment": 0.05, "follow": 0.5, "add_cart": 0.1, "purchase": 0.01},
    "Window Shopper": {"search": 0.3, "like": 0.8, "save": 0.5, "comment": 0.05, "follow": 0.2, "add_cart": 0.05, "purchase": 0.001},
    "Inbound Tourist": {"search": 0.8, "like": 0.4, "save": 0.85, "comment": 0.25, "follow": 0.45, "add_cart": 0.05, "purchase": 0.01},
    "Student Buyer": {"search": 0.6, "like": 0.4, "save": 0.5, "comment": 0.35, "follow": 0.2, "add_cart": 0.45, "purchase": 0.05},
    "Niche Hobbyist": {"search": 0.6, "like": 0.8, "save": 0.75, "comment": 0.4, "follow": 0.6, "add_cart": 0.3, "purchase": 0.15}
}

# Sinh Bot User
def generate_bot_users(num_users):
    users = []
    for _ in range(num_users):
        users.append({
            "user_id": str(uuid.uuid4()),
            "persona": random.choice(list(personas_config.keys()))
        })
    return users

# Sinh Seed Post
def generate_seed_posts(num_posts):
    posts = []
    categories = list(personas_config.keys())
    for _ in range(num_posts):
        posts.append({
            "post_id": str(uuid.uuid4()),
            "category": random.choice(categories) # Map post với category tương ứng
        })
    return posts

# 2. Logic Bot Simulation
def trigger_action(probability):
    """Hàm xác định xem bot có thực hiện hành động hay không dựa vào xác suất"""
    return random.random() < probability

def simulate_bot_interactions(users, posts, num_sessions_per_user=5):
    interaction_logs = []
    
    for user in users:
        persona_probs = personas_config[user["persona"]]
        
        for _ in range(num_sessions_per_user):
            session_id = str(uuid.uuid4())
            timestamp = datetime.datetime.now() - datetime.timedelta(days=random.randint(0, 30))
            
            # Bot mở Feed hoặc Search (Checklist)
            is_search = trigger_action(persona_probs["search"])
            source = "Search" if is_search else "Feed"
            
            # Lấy ngẫu nhiên vài post để xem trong session này
            viewed_posts = random.sample(posts, random.randint(3, 10))
            
            for post in viewed_posts:
                timestamp += datetime.timedelta(seconds=random.randint(10, 120)) # Thời gian dừng xem post
                
                # Ghi nhận Impression (Giống bảng post_impression_log)
                interaction_logs.append({
                    "user_id": user["user_id"],
                    "post_id": post["post_id"],
                    "action": "IMPRESSION",
                    "source": source,
                    "timestamp": timestamp
                })

                # Kiểm tra xem post có đúng gu (persona) không, nếu đúng gu thì tăng x1.5 xác suất tương tác
                affinity_multiplier = 1.5 if post["category"] == user["persona"] else 0.5
                
                # Bot Like, Save, Comment, Follow
                if trigger_action(min(persona_probs["like"] * affinity_multiplier, 0.99)):
                    interaction_logs.append({"user_id": user["user_id"], "post_id": post["post_id"], "action": "LIKE", "timestamp": timestamp})
                
                if trigger_action(min(persona_probs["save"] * affinity_multiplier, 0.99)):
                    interaction_logs.append({"user_id": user["user_id"], "post_id": post["post_id"], "action": "SAVE", "timestamp": timestamp})
                    
                if trigger_action(min(persona_probs["comment"] * affinity_multiplier, 0.99)):
                    interaction_logs.append({"user_id": user["user_id"], "post_id": post["post_id"], "action": "COMMENT", "timestamp": timestamp})
                
                # Bot Add Cart & Purchase
                if trigger_action(min(persona_probs["add_cart"] * affinity_multiplier, 0.99)):
                    interaction_logs.append({"user_id": user["user_id"], "post_id": post["post_id"], "action": "ADD_CART", "timestamp": timestamp})
                    
                    # Chỉ mua khi đã thêm vào giỏ hàng
                    if trigger_action(persona_probs["purchase"]):
                        timestamp += datetime.timedelta(minutes=random.randint(1, 15))
                        interaction_logs.append({"user_id": user["user_id"], "post_id": post["post_id"], "action": "PURCHASE", "timestamp": timestamp})

    return interaction_logs

# 3. Chạy Simulation & Xuất Dataset
if __name__ == "__main__":
    print("Khởi tạo Seed Data...")
    bot_users = generate_bot_users(100)  # Tạo 100 con bot
    seed_posts = generate_seed_posts(500) # Tạo 500 bài viết
    
    print("Bắt đầu Bot Simulation sinh tương tác...")
    logs = simulate_bot_interactions(bot_users, seed_posts, num_sessions_per_user=10)
    
    # Chuyển đổi thành DataFrame để dễ dàng phân tích hoặc ghi vào DB
    df_logs = pd.DataFrame(logs)
    print(f"Đã sinh thành công {len(df_logs)} dòng tương tác.")
    
    # Xem thử phân bổ hành động
    print(df_logs['action'].value_counts())
    
    # Lưu ra CSV (hoặc cấu hình đoạn code này đẩy thẳng vào PostgreSQL)
    df_logs.to_csv("bot_simulation_dataset.csv", index=False)