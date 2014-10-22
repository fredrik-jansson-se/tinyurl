
tmux kill-session -t tinyURL
tmux new -d -s tinyURL 
tmux send-keys -t tinyURL 'redis/src/redis-server' C-m
tmux split-window -v -t tinyURL 
tmux send-keys -t tinyURL 'sbt frontend/run' C-m
tmux split-window -h -t tinyURL
tmux send-keys -t tinyURL 'curl -d "URL=http://www.fredriks.se" http://localhost:8080/create'
tmux attach -t tinyURL
