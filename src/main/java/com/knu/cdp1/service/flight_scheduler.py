import json
import sys
import pulp

def calculate_weather_risk(wind_speed, rainfall, visibility):
    return (0.4 * wind_speed / 30) + (0.4 * rainfall / 30) + (0.2 * 500 / visibility)

def calculate_schedule(data):
    try:
        # 데이터를 JSON에서 Python 객체로 변환
        n = data['n']
        planned_start_times = data['planned_start_times']
        planned_end_times = data['planned_end_times']
        delayed_amount = data['delayed_amount']
        planned_end_times[0] += delayed_amount

        # 날씨 정보와 기타 데이터 설정
        weather_info = data['weather_info']
        weather_base = [calculate_weather_risk(info["wind_speed"], info["rainfall"], info["visibility"]) for info in weather_info]
        pass_num = data['pass_num']
        seat_cost = data['seat_cost']
        b = data['b']

        # 날씨 변화율 설정
        weather_change = [(weather_base[i+1] - weather_base[i]) / 4 for i in range(n-1)]
        weather_change.append(0)  # 마지막 값에 대해 기본값으로 0 추가

        Penalty_delay = [50 * pass_num[i] * seat_cost[i] for i in range(n)]
        Penalty_cancel = [500 * pass_num[i] * seat_cost[i] for i in range(n)]
        lambda_risk = data['lambda_risk']
        M = data['M']

        # 최적화 문제 정의
        problem = pulp.LpProblem("Flight_Scheduling_Minimization", pulp.LpMinimize)
        d = [pulp.LpVariable(f"d_{i}", lowBound=0, cat='Integer') for i in range(n)]
        z = [pulp.LpVariable(f"z_{i}", cat='Binary') for i in range(n)]

        # 목적 함수 정의
        problem += pulp.lpSum([
            Penalty_delay[i] * d[i] + Penalty_cancel[i] * z[i] + lambda_risk * weather_change[i] * d[i] for i in range(n)
        ])

        # 제약 조건 설정
        for i in range(n - 1):
            problem += planned_start_times[i] + d[i] + 3 <= planned_start_times[i + 1] + d[i + 1] + M * (z[i] + z[i + 1])
            problem += planned_end_times[i] + d[i] + 3 <= planned_end_times[i + 1] + d[i + 1] + M * (z[i] + z[i + 1])
        for i in range(n):
            problem += d[i] <= M * (1 - z[i])
        for i in range(n):
            if b[i] == 1:
                problem += z[i] == 0
        for i in range(n):
            if b[i] == 0:
                problem += (weather_base[i] + weather_change[i] * d[i]) <= 0.5

        # 문제 풀기
        problem.solve(pulp.PULP_CBC_CMD(msg=False))

        # 결과 저장
        results = []
        for i in range(n):
            delay = d[i].varValue
            cancelled = z[i].varValue

            if cancelled == 0:
                adjust_start_time = planned_start_times[i] + delay if b[i] != 1 else planned_start_times[i]
                adjust_end_time = planned_end_times[i] + delay
            else:
                adjust_start_time = " - "
                adjust_end_time = " - "

            cost = Penalty_delay[i] * delay + lambda_risk * weather_change[i] * delay if cancelled == 0 else Penalty_cancel[i]

            results.append({
                "planned_start_time": planned_start_times[i],
                "planned_end_time": planned_end_times[i],
                "adjusted_start_time": adjust_start_time,
                "adjusted_end_time": adjust_end_time,
                "delay": delay,
                "cancelled": cancelled,
                "cost": cost
            })


        # 결과를 JSON으로 반환
        return json.dumps(results)

    except Exception as e:
        sys.stderr.write(f"Error in calculate_schedule: {str(e)}\n")
        sys.exit(1)

# JSON 형식의 데이터를 받기
if __name__ == "__main__":
    try:
        input_data = json.loads(sys.stdin.read())
        result = calculate_schedule(input_data)
        print(result)  # JSON 데이터만 출력
    except Exception as e:
        sys.stderr.write(f"Error in main: {str(e)}\n")
        sys.exit(1)
