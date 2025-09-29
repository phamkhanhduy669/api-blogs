// Simple frontend logic for interacting with JWT API
const App = (() => {
  const API = {
    register: '/api/auth/register',
    login: '/api/auth/login',
    blogs: '/api/blogs'
  };

  function saveAuth(jwt, username, roles) {
    localStorage.setItem('jwt', jwt);
    localStorage.setItem('username', username);
    // store roles as JSON string
    localStorage.setItem('roles', JSON.stringify(roles || []));
    // convenience: primaryRole = first role without prefix trimming
    const primary = (roles && roles.length) ? roles[0] : '';
    localStorage.setItem('role', primary); // keep backward compatibility in UI
  }
  function clearAuth() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('roles');
  }
  function getToken() { return localStorage.getItem('jwt'); }
  function isLoggedIn() { return !!getToken(); }

  function showAlert(id, msg, type='error') {
    const el = document.getElementById(id);
    if(!el) return;
    el.classList.remove('hidden','error','success');
    el.classList.add('alert', type === 'success' ? 'success':'error');
    el.textContent = msg;
  }
  function hideAlert(id){
    const el = document.getElementById(id);
    if(el){ el.classList.add('hidden'); }
  }

  async function handleRegisterForm() {
    const form = document.getElementById('register-form');
    if(!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      hideAlert('register-alert');
      const data = Object.fromEntries(new FormData(form).entries());
      try {
        const res = await fetch(API.register, {
          method:'POST', headers:{'Content-Type':'application/json'},
          body: JSON.stringify(data)
        });
        const json = await res.json();
        if(!res.ok) throw new Error(json.message || 'Đăng ký thất bại');
        showAlert('register-alert','Đăng ký thành công!', 'success');
        setTimeout(()=> location.href='/login.html', 1200);
      } catch(err){
        showAlert('register-alert', err.message);
      }
    });
  }

  async function handleLoginForm() {
    const form = document.getElementById('login-form');
    if(!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      hideAlert('login-alert');
      const data = Object.fromEntries(new FormData(form).entries());
      try {
        const res = await fetch(API.login, {
          method:'POST', headers:{'Content-Type':'application/json'},
          body: JSON.stringify(data)
        });
        let json;
        const text = await res.text();
        try { json = text ? JSON.parse(text) : {}; } catch(parseErr){
          throw new Error('Phản hồi không hợp lệ từ server: '+ text.substring(0,200));
        }
        if(!res.ok || !json.token) throw new Error(json.message || 'Đăng nhập thất bại');
        const roles = json.roles || [];
        saveAuth(json.token, json.username, roles);
        document.getElementById('token-box').classList.remove('hidden');
        document.getElementById('token-box').textContent = json.token;
        showAlert('login-alert','Đăng nhập thành công!', 'success');
        setTimeout(()=> location.href='/blogs.html', 1000);
      } catch(err){
        showAlert('login-alert', err.message);
      }
    });
  }

  function logoutLink(){
    const link = document.getElementById('logout-link');
    if(link){
      link.addEventListener('click', (e)=>{ e.preventDefault(); clearAuth(); location.href='/login.html'; });
    }
  }

  async function loadBlogs(){
    const list = document.getElementById('blogs-list');
    if(!list) return;
    list.innerHTML = 'Đang tải...';
    try {
      const res = await fetch(API.blogs, { headers: { 'Authorization': 'Bearer ' + getToken() }});
      if(res.status === 401) { list.innerHTML = 'Chưa đăng nhập'; return; }
      const blogs = await res.json();
      list.innerHTML = '';
      if(!Array.isArray(blogs) || blogs.length === 0){
        list.innerHTML = '<p>Chưa có blog.</p>'; return;
      }
      blogs.forEach(b => list.appendChild(renderBlog(b)));
    } catch(err){
      list.innerHTML = '<p>Lỗi tải blog: '+err.message+'</p>';
    }

    // If admin, show user management section
    const role = (localStorage.getItem('role')||'').replace('ROLE_','');
    if(role === 'ADMIN') {
      document.getElementById('admin-users-section')?.classList.remove('hidden');
      loadUsers();
    }
  }

  async function loadUsers(){
    const list = document.getElementById('users-list');
    if(!list) return;
    list.innerHTML = 'Đang tải...';
    hideAlert('users-alert');
    try {
      const res = await fetch('/api/users', { headers: { 'Authorization': 'Bearer ' + getToken() }});
      if(!res.ok) throw new Error('Không thể tải danh sách user');
      const users = await res.json();
      list.innerHTML = '';
      if(!Array.isArray(users) || users.length === 0){
        list.innerHTML = '<p>Không có user nào.</p>'; return;
      }
      users.forEach(u => list.appendChild(renderUser(u)));
    } catch(err){
      showAlert('users-alert', err.message);
      list.innerHTML = '';
    }
  }

  function renderUser(u){
    const div = document.createElement('div');
    div.className = 'user-row';
    div.innerHTML = `<b>${escapeHtml(u.username)}</b> | Roles: ${u.roles.map(r=>r.replace('ROLE_','')).join(', ')} | Blogs: ${u.blogCount}`;
    if(u.roles.includes('ROLE_ADMIN')) {
      div.innerHTML += ' <span style="color:gray">(admin)</span>';
    } else {
      const delBtn = document.createElement('button');
      delBtn.textContent = 'Xoá';
      delBtn.className = 'danger';
      delBtn.onclick = ()=> deleteUser(u.username);
      div.appendChild(delBtn);
    }
    return div;
  }

  async function deleteUser(username){
    if(!confirm('Xoá user này?')) return;
    try {
      const res = await fetch('/api/users/'+username, { method:'DELETE', headers:{'Authorization':'Bearer '+getToken()} });
      const json = await res.json();
      if(!res.ok) throw new Error(json.message || 'Xoá thất bại');
      showAlert('users-alert','Xoá thành công','success');
      loadUsers();
    } catch(err){ showAlert('users-alert', err.message); }
  }

  function renderBlog(b){
    const card = document.createElement('div');
    card.className = 'blog-card';
    card.innerHTML = `<h3>${escapeHtml(b.title)}</h3><p>${escapeHtml(b.content)}</p><small>Tác giả: ${b.authorUsername}</small>`;
    const actions = document.createElement('div');
    actions.className='blog-actions';
    const currentUser = localStorage.getItem('username');
  const role = (localStorage.getItem('role')||'').replace('ROLE_','');
  if(b.authorUsername === currentUser || role === 'ADMIN') {
      const editBtn = document.createElement('button'); editBtn.textContent='Sửa';
      editBtn.onclick = ()=> editBlogPrompt(b);
      const delBtn = document.createElement('button'); delBtn.textContent='Xoá'; delBtn.className='danger';
      delBtn.onclick = ()=> deleteBlog(b.id);
      actions.appendChild(editBtn); actions.appendChild(delBtn);
    }
    card.appendChild(actions);
    return card;
  }

  function escapeHtml(str){ return str.replace(/[&<>"]+/g, c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }

  function editBlogPrompt(blog){
    const title = prompt('Tiêu đề mới', blog.title); if(title===null) return;
    const content = prompt('Nội dung mới', blog.content); if(content===null) return;
    updateBlog(blog.id, { title, content });
  }

  async function updateBlog(id, data){
    try {
      const res = await fetch(API.blogs+'/'+id, { method:'PUT', headers:{'Content-Type':'application/json','Authorization':'Bearer '+getToken()}, body: JSON.stringify(data)});
      if(!res.ok) throw new Error('Cập nhật thất bại');
      loadBlogs();
    } catch(err){ showAlert('blogs-alert', err.message); }
  }

  async function deleteBlog(id){
    if(!confirm('Xoá blog?')) return;
    try {
      const res = await fetch(API.blogs+'/'+id, { method:'DELETE', headers:{'Authorization':'Bearer '+getToken()} });
      if(!res.ok) throw new Error('Xoá thất bại');
      loadBlogs();
    } catch(err){ showAlert('blogs-alert', err.message); }
  }

  function handleCreateBlog(){
    const form = document.getElementById('create-blog-form');
    if(!form) return;
    form.addEventListener('submit', async (e)=>{
      e.preventDefault(); hideAlert('blogs-alert');
      const data = Object.fromEntries(new FormData(form).entries());
      try {
        const res = await fetch(API.blogs, { method:'POST', headers:{'Content-Type':'application/json','Authorization':'Bearer '+getToken()}, body: JSON.stringify(data)});
        if(!res.ok) throw new Error('Tạo blog thất bại');
        form.reset(); loadBlogs(); showAlert('blogs-alert','Tạo thành công','success');
      } catch(err){ showAlert('blogs-alert', err.message); }
    });
  }

  function displayAuthInfo(){
    const box = document.getElementById('auth-info');
    if(!box) return;
    if(isLoggedIn()) {
  const role = (localStorage.getItem('role')||'').replace('ROLE_','');
  box.textContent = 'Logged in as '+localStorage.getItem('username')+' ('+role+')';
      document.getElementById('create-blog-section')?.classList.remove('hidden');
    } else {
      box.innerHTML = 'Bạn chưa đăng nhập. <a href="/login.html">Đăng nhập</a>';
    }
  }

  function initLogin(){ handleLoginForm(); }
  function initRegister(){ handleRegisterForm(); }
  function initBlogs(){ if(!isLoggedIn()) displayAuthInfo(); else { displayAuthInfo(); loadBlogs(); handleCreateBlog(); } logoutLink(); }

  return { initLogin, initRegister, initBlogs };
})();

window.App = App;