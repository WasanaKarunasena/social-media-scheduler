import { useState, useEffect } from "react";
import axios from "axios";
import "./styles.css";

export default function Home() {
  const [content, setContent] = useState("");
  const [datetime, setDatetime] = useState("");
  const [image, setImage] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [posts, setPosts] = useState([]);
  const [editingId, setEditingId] = useState(null);

  const fetchPosts = async () => {
    try {
      const res = await axios.get("http://localhost:8080/api/posts");
      setPosts(res.data);
    } catch (err) {
      console.error("Failed to fetch posts", err);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  const resetForm = () => {
    setContent("");
    setDatetime("");
    setImage(null);
    setPreviewUrl(null);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      formData.append("content", content);
      formData.append("scheduledAt", datetime);
      if (image) {
        formData.append("image", image);
      }

      if (editingId) {
        await axios.put(
          `http://localhost:8080/api/posts/${editingId}`,
          formData,
          { headers: { "Content-Type": "multipart/form-data" } }
        );
      } else {
        await axios.post("http://localhost:8080/api/posts", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      }

      resetForm();
      fetchPosts();
    } catch (err) {
      console.error("Failed to submit post", err);
    }
  };

  const handleEdit = (post) => {
    setContent(post.content);
    setDatetime(post.scheduledAt ? post.scheduledAt.slice(0, 16) : "");
    setEditingId(post.id);
    setPreviewUrl(post.imageUrl ? `http://localhost:8080${post.imageUrl}` : null);
    setImage(null);
  };

  const handleDelete = async (id) => {
    try {
      await axios.delete(`http://localhost:8080/api/posts/${id}`);
      if (editingId === id) {
        resetForm();
      }
      fetchPosts();
    } catch (err) {
      console.error("Failed to delete post", err);
    }
  };

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setImage(e.target.files[0]);
      setPreviewUrl(URL.createObjectURL(e.target.files[0]));
    } else {
      setImage(null);
      setPreviewUrl(null);
    }
  };

  return (
    <div className="container">
      <h1 className="title">Schedule Social Media Post</h1>
      <form onSubmit={handleSubmit} className="form" encType="multipart/form-data">
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="What's on your mind?"
          className="textarea"
          required
        />
        <input
          type="datetime-local"
          value={datetime}
          onChange={(e) => setDatetime(e.target.value)}
          className="input"
          required
        />
        <input type="file" accept="image/*" onChange={handleImageChange} />
        {previewUrl && (
          <img
            src={previewUrl}
            alt="Preview"
            style={{ maxWidth: "200px", marginTop: "10px", borderRadius: "6px" }}
          />
        )}
        <button className="button">{editingId ? "Update Post" : "Schedule"}</button>
      </form>

      <h2 className="subtitle">All Posts</h2>
      <ul className="post-list">
        {/* Header Row */}
        <li className="post header-row">
          <div>Content</div>
          <div>Image</div>
          <div>Scheduled At</div>
          <div>Status</div>
          <div>Actions</div>
        </li>

        {posts.map((post) => (
          <li key={post.id} className="post">
            <div className="post-content">{post.content}</div>

            <div className="post-image">
              {post.imageUrl && (
                <img
                  src={`http://localhost:8080${post.imageUrl}`}
                  alt="Post"
                />
              )}
            </div>

            <div className="post-scheduled">
              {new Date(post.scheduledAt).toLocaleString()}
            </div>

            <div className="post-status" style={{ color: post.published ? 'green' : 'orange' }}>
              {post.published
                ? `✅ Published at ${new Date(post.publishedAt).toLocaleString()}`
                : "🕒 Pending"}
            </div>

            <div className="post-actions">
              {!post.published && (
                <button
                  onClick={() => handleEdit(post)}
                  className="edit-btn"
                >
                  Edit
                </button>
              )}
              <button
                onClick={() => handleDelete(post.id)}
                className="delete-btn"
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
